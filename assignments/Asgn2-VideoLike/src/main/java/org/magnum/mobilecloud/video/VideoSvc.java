package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import retrofit.http.Path;


/**
 * This simple VideoSvc allows clients to send HTTP POST requests with
 * videos that are stored in memory using a list. Clients can send HTTP GET
 * requests to receive a JSON listing of the videos that have been sent to
 * the controller so far. Stopping the controller will cause it to lose the history of
 * videos that have been sent to it because they are stored in memory.
 * 
 * Notice how much simpler this VideoSvc is than the original VideoServlet?
 * Spring allows us to dramatically simplify our service. Another important
 * aspect of this version is that we have defined a VideoSvcApi that provides
 * strong typing on both the client and service interface to ensure that we
 * don't send the wrong paraemters, etc.
 * 
 * @author jules
 *
 */

// Tell Spring that this class is a Controller that should 
// handle certain HTTP requests for the DispatcherServlet
@Controller
public class VideoSvc {
	
	// The VideoRepository that we are going to store our videos
	// in. We don't explicitly construct a VideoRepository, but
	// instead mark this object as a dependency that needs to be
	// injected by Spring. Our Application class has a method
	// annotated with @Bean that determines what object will end
	// up being injected into this member variable.
	//
	// Also notice that we don't even need a setter for Spring to
	// do the injection.
	//
	private List<String> likeGroup = new ArrayList<String>(); 
	
	@Autowired
	private VideoRepository videos;
	
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method=RequestMethod.POST)
	public void likeVideo(@PathVariable("id") long id, 
			              HttpServletResponse response,
			              Principal p){
		 String userName = p.getName();
		 if (videos.exists(id)){
			 if (likeGroup.contains(userName)){				 
				 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 } else {
				 likeGroup.add(userName);
			     Video v = videos.findOne(id);
			     v.setLikes(v.getLikes()+1);
			     videos.save(v);
			     response.setStatus(HttpServletResponse.SC_OK);
			 }
		 } else {
			 response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		 }
	}
		
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method=RequestMethod.POST)
	public void unlikeVideo(@PathVariable("id") long id,
			                HttpServletResponse response,
                            Principal p){
		String userName = p.getName();
		if (videos.exists(id)){
			 if (!likeGroup.contains(userName)){				 
				 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 } else {
				 likeGroup.remove(userName);
			     Video v = videos.findOne(id);
			     v.setLikes(v.getLikes()-1);
			     videos.save(v);
			     response.setStatus(HttpServletResponse.SC_OK);
			 }
		 } else {
			 response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		 }
		
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method=RequestMethod.GET)
	public @ResponseBody List<String> getUsersWhoLikedVideo(@PathVariable("id") long id,
			                                                HttpServletResponse response){
		if (videos.exists(id)){
			 return likeGroup;
		 } else {
			 response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			 return new ArrayList<String>();
		 }
        
	}
	
}
