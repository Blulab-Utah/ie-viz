package edu.utah.blulab.services;

import edu.pitt.dbmi.nlp.noble.ui.NobleMentionsTool;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NoblementionsConnector implements INoblementionsConnector {
    @Override
    public void processNobleMentions(Map<String, String> pathMap) throws Exception {
        NobleMentionsTool nobleMentionsTool = new NobleMentionsTool();
        nobleMentionsTool.ievizProcess(pathMap);

    }
}
