package com.jerry.dynaviewer.app;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by jerry on 4/29/2014.
 */
public class DynaCard {

    // attributes for loads and positions
    final String slPrefix = "SL.i.";
    final String spPrefix = "SP.i.";
    final String dlPrefix = "DL.i.";
    final String dpPrefix = "DP.i.";

    public class LoadPosPair {

        public final float load;
        public final float pos;

        public LoadPosPair(float _load, float _pos) {
            load = _load;
            pos = _pos;
        }
    }

    public Map<String, String> Attributes = new HashMap<String, String>();
    public ArrayList<LoadPosPair> DownholePoints = new ArrayList<LoadPosPair>();
    public ArrayList<LoadPosPair> SurfacePoints = new ArrayList<LoadPosPair>();

    public final String XmlText;

    DynaCard(String xmlText) {
        XmlText = xmlText;
    }

    // TODO: Change to sparse array
    final Map<Integer, Float> surfaceLoads = new HashMap<Integer, Float>();
    final Map<Integer, Float> surfacePositions = new HashMap<Integer, Float>();
    final Map<Integer, Float> downholeLoads = new HashMap<Integer, Float>();
    final Map<Integer, Float> downholePositions = new HashMap<Integer, Float>();

    void Load() throws ParserConfigurationException, SAXException, IOException {

        SAXParserFactory saxPF = SAXParserFactory.newInstance();
        SAXParser saxP = saxPF.newSAXParser();
        XMLReader xmlR = saxP.getXMLReader();

        XmlHandler xmlHandler = new XmlHandler();
        xmlR.setContentHandler(xmlHandler);
        xmlR.parse(new InputSource(new StringReader(XmlText)));

        // done parsing. process surface and downhole points
        assert (surfaceLoads.size() == surfacePositions.size());
        assert (downholeLoads.size() == downholePositions.size());

        SurfacePoints = new ArrayList<LoadPosPair>(surfaceLoads.size());

        for (Map.Entry<Integer, Float> entry : surfaceLoads.entrySet()) {
            Integer index = entry.getKey();
            assert (surfacePositions.containsKey(index));
            Float load = entry.getValue();
            Float pos = surfacePositions.get(index);
            SurfacePoints.add(new LoadPosPair(load, pos));
        }

        DownholePoints = new ArrayList<LoadPosPair>(downholeLoads.size());

        for (Map.Entry<Integer, Float> entry : downholeLoads.entrySet()) {
            Integer index = entry.getKey();
            assert (downholePositions.containsKey(index));
            Float load = entry.getValue();
            Float pos = downholePositions.get(index);
            DownholePoints.add(new LoadPosPair(load, pos));
        }
    }

    class XmlHandler extends DefaultHandler {

        Boolean dgDataNodeFound = false;

        String elementName = null;
        String elementText = null;
        Map<String, String> elementAttributes = new HashMap<String, String>();

        /**
         * called on start of xml element
         */
        @Override
        public void startElement(String uri, String localName, String qName,
                                 org.xml.sax.Attributes attributes) throws SAXException {

            // for some reason localName is blank. If so use qName

            String name = localName.equals("") ? qName : localName;

            // only process nodes under the dgData node
            if (!dgDataNodeFound) {
                dgDataNodeFound = name.equals("dgData");
                return;
            }

            // store the name of the element for later sanity check
            elementName = name;

            // store away the attributes
            for (int i = 0; i < attributes.getLength(); i++) {
                String tag = attributes.getLocalName(i);
                String value = attributes.getValue(i);
                elementAttributes.put(tag, value);
            }
        }

        //  Parse a string in CygNet "dotted i" format
        //  example: <SL.i.0 dt:dt="ui2">0</SL.i.0>
        private void AddFromDottedI(Map<Integer, Float> map, String name, String text, String prefix) {
            String indexText = name.substring(prefix.length());
            Integer index = Integer.parseInt(indexText);
            float value = Float.parseFloat(text);
            assert (!map.containsKey(index));
            map.put(index, value);
        }

        // called on CDATA (text) values
        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {

            // store the element text if processing an element
            if (elementName!=null) {
                elementText = new String(ch, start, length);
            }
        }

        /**
         * called on end of xml element. Do the actual processing here when have
         * all the information.
         */
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {

            // do nothing if not processing an element
            if (elementName == null) {
                return;
            }

            // get the name using same logic about blank localName as on startElement
            String name = localName.equals("") ? qName : localName;

            // the name should be the same. It would not be in the case of nested nodes
            // but do not handle that
            if (!elementName.equals(name)) {
                throw new RuntimeException("name mismatch on endElement: " + elementName + "!=" + name);
            }

            if (elementName.startsWith(slPrefix)) {
                AddFromDottedI(surfaceLoads, elementName, elementText, slPrefix);
            } else if (elementName.startsWith(spPrefix)) {
                AddFromDottedI(surfacePositions, elementName, elementText, spPrefix);
            } else if (elementName.startsWith(dlPrefix)) {
                AddFromDottedI(downholeLoads, elementName, elementText, dlPrefix);
            } else if (elementName.startsWith(dpPrefix)) {
                AddFromDottedI(downholePositions, elementName, elementText, dpPrefix);
            } else {
                Attributes.put(elementName, elementText);
            }

            elementName = null;
        }
    }
}
