package com.hirw.facebookfriends.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.hirw.facebookfriends.writables.Friend;
import com.hirw.facebookfriends.writables.FriendArray;
import com.hirw.facebookfriends.writables.FriendPair;

public class FacebookFriendsMapper extends Mapper<LongWritable, Text, FriendPair, FriendArray> {
	
	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		
		Logger log = Logger.getLogger(FacebookFriendsMapper.class);
		
		// the text is made with {"person"}\t[{person},{person}], where \t is tab
		StringTokenizer st = new StringTokenizer(value.toString(), "\t"); // this split for \t
		String person = st.nextToken(); // the first token is the person
		String friends = st.nextToken(); // then the list of friends
		
		// populate a Friend object using the first token
		Friend f1 = populateFriend(person);
		
		// after we have to populate the list of friends of a person
		List<Friend> friendList = populateFriendList(friends);
		
		// after create the list of friends, convert it to an array
		Friend[] friendArray = Arrays.copyOf(friendList.toArray(), friendList.toArray().length, Friend[].class);
		
		// after convert the list into an array, we can init a FriendArray object
		FriendArray farray = new FriendArray(Friend.class, friendArray);
		
		// the iterate through the list of friends
		for(Friend f2 : friendList) {
			// make a couple person-friend
			// taken from the list of friends
			FriendPair fpair = new FriendPair(f1, f2);
			context.write(fpair, farray);
			
			// remember that this command send the <key,value> pair
			// to Hadoop; the shuffle will get all the outputs and 
			// compare them by key: the key is a FriendPair, in which
			// we have overwrite the compareTo method, in order to have
			// true when we have ("a","b") and ("b","a"), in order to aggregate
			// the keys without considering the order of the two elements
			log.info(fpair+"......"+ farray);
		}

		
	}
	
	private Friend populateFriend(String friendJson) {
		
		
		JSONParser parser = new JSONParser();
		Friend friend = null;
		try {
			
			// parse the json string and map it to an object
			Object obj = (Object)parser.parse(friendJson);
			// then cast the object to a JSONObject
			JSONObject jsonObject = (JSONObject) obj;

			// once we have the JSONObject, we can get the
			// properties of the json
			Long lid = (long)jsonObject.get("id");
			IntWritable id = new IntWritable(lid.intValue());
			Text name = new Text((String)jsonObject.get("name"));
			Text hometown = new Text((String)jsonObject.get("hometown"));
			
			// and we can use them to init a new Friend object
			friend = new Friend(id, name, hometown);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return friend;
	}
	
	private List<Friend> populateFriendList(String friendsJson) {
		
		List<Friend> friendList = new ArrayList<Friend>();
		
		try {
			// the first part is the same of population
			// of Friend object
			JSONParser parser = new JSONParser();
			Object obj = (Object)parser.parse(friendsJson.toString());
			
			// this time we are not casting to a JSONObject
			// but to a JSONArray
			JSONArray jsonarray = (JSONArray) obj;

			// then we iterate through the array
			for(Object jobj : jsonarray) {
				// cast the iterator to a JSONObjec
				JSONObject entry = (JSONObject)jobj;
				
				// and made the same things done for 
				// populating the Friend object
				Long lid = (long)entry.get("id");
				IntWritable id = new IntWritable(lid.intValue());
				Text name = new Text((String)entry.get("name"));
				Text hometown = new Text((String)entry.get("hometown"));
				Friend friend = new Friend(id, name, hometown);
				
				// at the end we add the Friend to the return list
				friendList.add(friend);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return friendList;
	}

}
