package com.criz.metaDataEdit;

import com.acrcloud.utils.ACRCloudRecognizer;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MetaDataEditApplication {
	public static void main(String[] args) throws ParseException {
		String result = apiCall("D:\\dev\\metaDataEdit\\test\\freeky.mp3", 20);

		try {
			JSONObject json = parseToJson(result);

			String trackName = getTrackName(json);
			String artist = getArtistName(json);

			metadataEdit("D:\\dev\\metaDataEdit\\test\\freeky.mp3", artist, trackName, "album");
		} catch (Exception e) {
			System.out.println("Exception while reading file: " + e.getMessage());
		}
	}

	static JSONObject parseToJson(String string) throws Exception {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(string);
	}
	static String apiCall(String musicFileName, int startSeconds) {
		Map<String, Object> config = new HashMap<String, Object>();

		config.put("host", "identify-eu-west-1.acrcloud.com");
		config.put("access_key", "cb31c493bfc4b85bf2629ac7839021d2");
		config.put("access_secret", "wjn9plBaOkJLp2g2kZQrK27CXGQjouKn3pEigxyU");

		config.put("debug", false);
		config.put("timeout", 10); // seconds

		ACRCloudRecognizer re = new ACRCloudRecognizer(config);

		// It will skip 80 seconds.
		String result = re.recognizeByFile(musicFileName, startSeconds);
		System.out.println(result);

		/**
		 *  recognize by buffer of (Formatter: Audio/Video)
		 *     Audio: mp3, wav, m4a, flac, aac, amr, ape, ogg ...
		 **/

		File file = new File(musicFileName);
		byte[] buffer = new byte[3 * 1024 * 1024];
		if (!file.exists()) {
			return "";
		}
		FileInputStream fin = null;
		int bufferLen = 0;
		try {
			fin = new FileInputStream(file);
			bufferLen = fin.read(buffer, 0, buffer.length);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fin != null) {
					fin.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("bufferLen=" + bufferLen);

		if (bufferLen <= 0)
			return "";

		// It will skip 80 seconds from the begginning of (buffer).
		result = re.recognizeByFileBuffer(buffer, bufferLen, 80);
		System.out.println(result);
		return result;
	}
	static void metadataEdit(String audioFilePath, String artist, String trackName, String album) throws Exception {
		File audioFile = new File(audioFilePath);
		MP3File fileToRead = (MP3File) AudioFileIO.read(audioFile);

		// Reading stuff ------------------------------------------------------------------------------
		AudioHeader audioHeader = fileToRead.getAudioHeader();
		int trackLength = audioHeader.getTrackLength();	// seconds
		System.out.println("track length: " + trackLength);

		// Writing stuff ------------------------------------------------------------------------------
		AudioFile fileToWrite = AudioFileIO.read(audioFile);
		Tag tag = fileToWrite.getTag();

		// TODO: Genre, label, year, titleNumber
		tag.setField(FieldKey.ARTIST, artist);
		tag.setField(FieldKey.TITLE, trackName);
		tag.setField(FieldKey.ALBUM, album);
		AudioFileIO.write(fileToWrite);
	}
	static String getTrackName(JSONObject json) {
		JSONObject metadata = (JSONObject) json.get("metadata");;
		JSONArray musicArray = (JSONArray) metadata.get("music");
		JSONObject music0 = (JSONObject) musicArray.get(0);
		JSONObject externalMetadata = (JSONObject) music0.get("external_metadata");
		JSONObject deezer = (JSONObject) externalMetadata.get("deezer");
		JSONObject track = (JSONObject) deezer.get("track");

		return track.get("name").toString();
	}
	static String getArtistName(JSONObject json) {
		JSONObject metadata = (JSONObject) json.get("metadata");;
		JSONArray musicArray = (JSONArray) metadata.get("music");
		JSONObject music0 = (JSONObject) musicArray.get(0);
		JSONObject externalMetadata = (JSONObject) music0.get("external_metadata");
		JSONObject deezer = (JSONObject) externalMetadata.get("deezer");
		JSONArray artists = (JSONArray) deezer.get("artists");
		JSONObject artist0 = (JSONObject) artists.get(0);

		return artist0.get("name").toString();
	}
}
