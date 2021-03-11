/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class VideoController {
	private static final AtomicLong currentId = new AtomicLong(0L);

	private Map<Long, Video> videos = new HashMap<Long, Video>();

	@RequestMapping(path= "/video", method= RequestMethod.GET)
	public @ResponseBody Collection<Video> returnVideoList() {
		return videos.values();
	}
	
	@RequestMapping(path= "/video", method= RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){
		checkAndSetId(v);
		v.setDataUrl(getDataUrl(v.getId()));
		videos.put(v.getId(), v);
		return v;
	}

	@RequestMapping(path= "/video/{id}/data", method= RequestMethod.POST)
	public @ResponseBody VideoStatus getAttr(@PathVariable(value="id") long id, @RequestParam("data") MultipartFile data) throws IOException{
		Video video  = videos.get(id);
		if( video == null) {
	        throw new ResponseStatusException(
	          HttpStatus.NOT_FOUND);
	    }
		saveSomeVideo(video, data);
		return new VideoStatus(VideoState.READY);
	}
	
	@RequestMapping("/video/{id}/data")
	public void downloadVideo(HttpServletResponse response, @PathVariable(value= "id") long id) throws IOException {
		Video video  = videos.get(id);
		if( video == null) {
	        throw new ResponseStatusException(
	          HttpStatus.NOT_FOUND);
	    }
		serveSomeVideo(video,response);
	}
	

	public Video save(Video entity) {
		checkAndSetId(entity);
		videos.put(entity.getId(), entity);
		return entity;
	}

	private void checkAndSetId(Video entity) {
		if(entity.getId() == 0){
			entity.setId(currentId.incrementAndGet());
		}
	}
	private String getDataUrl(long videoId){
		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
		return url;
	}
	private String getUrlBaseForLocalServer() {
		HttpServletRequest request =
				((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String base =
				"http://"+request.getServerName()
						+ ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		return base;
	}
	
    // You would need some Controller method to call this...
  	public void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
  	     VideoFileManager.get().saveVideoData(v, videoData.getInputStream());
  	}
  	
  	public void serveSomeVideo(Video v, HttpServletResponse response) throws IOException {
  	     // Of course, you would need to send some headers, etc. to the
  	     // client too!
  	     //  ...
  	     VideoFileManager.get().copyVideoData(v, response.getOutputStream());
  	}


	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	
}
