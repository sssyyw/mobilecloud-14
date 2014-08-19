package org.magnum.dataup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class VideoCtrl {
	
	private static final AtomicLong currentId = new AtomicLong(0L);
    private Map<Long,Video> videos = new HashMap<Long, Video>();
    private VideoFileManager videoDataMgr;

	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody List<Video> getVideoList(){
		return new ArrayList<Video>(videos.values());
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){
		checkAndSetId(v);
		//generate data url for the video
		v.setLocation(getDataUrl(v.getId()));
		videos.put(v.getId(), v);
		return v;
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus addVideoData(@PathVariable("id") long videoId,
			                                     @RequestParam("data") MultipartFile videoData,
			                                     HttpServletResponse response){
		if (!videos.containsKey(videoId)){
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		Video loadingVideo = videos.get(videoId);	
		
		
		try {
			videoDataMgr = VideoFileManager.get();
			saveSomeVideo(loadingVideo, videoData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    

		
		return new VideoStatus(VideoState.READY);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
	public void getVideoData(@PathVariable("id") long videoId,
			                                      HttpServletResponse response
			                                     ){
		if (!videos.containsKey(videoId)){
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Video v = videos.get(videoId);
		try {
			videoDataMgr = VideoFileManager.get();
			if (videoDataMgr.hasVideoData(v)){
			    serveSomeVideo(v, response);
			    //return response;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	    
		
		//return response;
	}
	
	// You would need some Controller method to call this...
    public void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
         videoDataMgr.saveVideoData(v, videoData.getInputStream());
    }

    public void serveSomeVideo(Video v, HttpServletResponse response) throws IOException {
         // Of course, you would need to send some headers, etc. to the
         // client too!
         //  ...
         videoDataMgr.copyVideoData(v, response.getOutputStream());
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
    
    private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }

}
