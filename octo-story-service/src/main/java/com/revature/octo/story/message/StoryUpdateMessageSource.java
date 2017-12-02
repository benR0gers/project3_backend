package com.revature.octo.story.message;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface StoryUpdateMessageSource {
	@Output("updatestorymessage")
	MessageChannel updateStoryMessagePlace();
	

}