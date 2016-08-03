package com.hirw.facebookfriends.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.mapreduce.Reducer;

import com.hirw.facebookfriends.writables.Friend;
import com.hirw.facebookfriends.writables.FriendArray;
import com.hirw.facebookfriends.writables.FriendPair;

public class FacebookFriendsReducer extends 
	Reducer<FriendPair, FriendArray, FriendPair, FriendArray> {
	
	@Override
	public void reduce(FriendPair key, Iterable<FriendArray> values, Context context)
					throws IOException, InterruptedException {
		
		List<Friend[]> flist = new ArrayList<Friend[]>();
		List<Friend> commonFriendsList = new ArrayList<Friend>();
		int count = 0;
		
		for(FriendArray farray : values) {
			// the combiners has aggregate by key
			// so we will have an input object like
			// <key,List<FriendArray>>
			
			// with this command we will copy the FriendArray
			// to a new array of Friend (remember in the 
			// mapper we have done the opposite)
			Friend[] f = Arrays.copyOf(farray.get(), farray.get().length, Friend[].class);
			
			// once we have the array of Friend we will add it
			// to a list
			flist.add(f);
			
			// and increase a counter that will serve to
			// a check
			count++;
		}
		
		// we are checking that we will not have a
		// list with a number different of two elements,
		// this because the key is made by a "COUPLE" of
		// friends, so the value in input at the
		// reducer will have to has exactly two list of
		// friends, one for the first person, and one for
		// the second person
		if(count != 2)
			return;
		
		// at the end we will perform a double loop,
		// in order to compare the elements of both
		// the array of Friend of the two people
		for(Friend outerf : flist.get(0)) {
			for(Friend innerf : flist.get(1)) {
				// if the two objects (friends)
				// are in common (the match is
				// done by the overriding
				if(outerf.equals(innerf))
					commonFriendsList.add(innerf);
			}
		}
		
		// after identification of the list
		// of common friends, we must convert
		// them into an array of Friend
		Friend[] commonFriendsArray = Arrays.copyOf(commonFriendsList.toArray(), commonFriendsList.toArray().length, Friend[].class);
		
		// and let Hadoop manage them after have
		// converted the array of friend into a
		// FriendArray object
		context.write(key, new FriendArray(Friend.class, commonFriendsArray));
	}


}
