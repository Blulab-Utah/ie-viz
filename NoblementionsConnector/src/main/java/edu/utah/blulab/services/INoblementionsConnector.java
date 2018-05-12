package edu.utah.blulab.services;

import java.util.Map;

public interface INoblementionsConnector {

    void processNobleMentions(Map<String,String> pathMap) throws Exception;
}
