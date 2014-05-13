package com.jerry.dynacard;

import java.io.ByteArrayInputStream;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by jerry on 4/29/2014.
 */
public class DynaCard {

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

    public final String xmlText;
    // TODO: Change to sparse array
    private final Map<Integer, Float> surfaceLoads = new HashMap<Integer, Float>();
    private final Map<Integer, Float> surfacePositions = new HashMap<Integer, Float>();
    private final Map<Integer, Float> downholeLoads = new HashMap<Integer, Float>();
    private final Map<Integer, Float> downholePositions = new HashMap<Integer, Float>();

    public DynaCard(String xmlText) {
        this.xmlText = xmlText;
    }

    public void Load() throws ParserConfigurationException, SAXException, IOException {
        Load(xmlText);
    }

    public void Load(String text) throws ParserConfigurationException, SAXException, IOException {
        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(text.getBytes());
        Load(is);
    }

    public void Load(InputStream in) throws ParserConfigurationException, SAXException, IOException {

        SAXParserFactory saxPF = SAXParserFactory.newInstance();
        SAXParser saxP = saxPF.newSAXParser();
        XMLReader xmlR = saxP.getXMLReader();

        XmlHandler xmlHandler = new XmlHandler();
        xmlR.setContentHandler(xmlHandler);
        xmlR.parse(new InputSource(in));

        // done parsing. process surface and downhole points
        assert (surfaceLoads.size() == surfacePositions.size());
        assert (downholeLoads.size() == downholePositions.size());

        SurfacePoints = new ArrayList<LoadPosPair>(surfaceLoads.size());

        for (Integer i=0; i<surfaceLoads.size(); i++) {
            try
            {
                Float pos = surfacePositions.get(i);
                Float load = surfaceLoads.get(i);
                SurfacePoints.add(new LoadPosPair(load, pos));
            }
            catch (Exception ex)
            {
                throw new NoSuchElementException("SL/SP " + i.toString());
            }
        }

        DownholePoints = new ArrayList<LoadPosPair>(downholeLoads.size());

        for (Integer i=0; i<downholeLoads.size(); i++) {
            try
            {
                Float pos = downholePositions.get(i);
                Float load = downholeLoads.get(i);
                DownholePoints.add(new LoadPosPair(load, pos));
            }
            catch (Exception ex)
            {
                throw new NoSuchElementException("DL/DP " + i.toString());
            }
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

            String name = localName.equals("") ? qName : localName;
            //System.out.println("startElement: name=" + name);

            if (!dgDataNodeFound) {
                dgDataNodeFound = name.equals("dgData");

                return;
            }

            elementName = name;

            for (int i = 0; i < attributes.getLength(); i++) {
                String tag = attributes.getLocalName(i);
                String value = attributes.getValue(i);
                elementAttributes.put(tag, value);
            }
        }

        // <SL.i.0 dt:dt="ui2">0</SL.i.0>
        private void AddFromDottedI(Map<Integer, Float> map, String name, String text, String prefix) {
            //

            String indexText = name.substring(prefix.length());
            Integer index = Integer.parseInt(indexText);
            float value = Float.parseFloat(text);
            assert (!map.containsKey(index));
            map.put(index, value);
        }

        /**
         * called with CDATA
         */
        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {

            if (dgDataNodeFound) {
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

            String name = localName.equals("") ? qName : localName;
            //System.out.println("endElement: name=" + name);

            if (elementName == null) {
                return;
            }

            // as do not handle nested parsing name must be the current one
            assert (elementName.equals(name));

            final String slPrefix = "SL.i.";
            final String spPrefix = "SP.i.";
            final String dlPrefix = "DL.i.";
            final String dpPrefix = "DP.i.";

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
        }
    }
}
