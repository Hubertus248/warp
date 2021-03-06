package net.warpgame.engine.ai.loader;

import org.xml.sax.SAXException;
import net.warpgame.engine.core.context.EngineContext;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Hubertus
 * Created 19.01.2017
 */
public class BehaviourTreeLoader {

    public static BehaviourTreeBuilder loadXML(String path) throws BehaviourTreeLoadException {
        try {
            InputStream stream = new FileInputStream(new File(EngineContext.CODESOURCE_DIR + path));
            SAXParserFactory factory = SAXParserFactory.newInstance();
            BehaviourTreeBuilder behaviourTreeBuilder = new BehaviourTreeBuilder();
            SAXParser saxParser = factory.newSAXParser();
            XMLHandler handler = new XMLHandler(behaviourTreeBuilder);
            saxParser.parse(stream, handler);
            return behaviourTreeBuilder;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new BehaviourTreeLoadException(e);
        }
    }
}
