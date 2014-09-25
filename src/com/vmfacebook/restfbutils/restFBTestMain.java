package com.vmfacebook.restfbutils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.restfb.DefaultFacebookClient;
import com.restfb.Facebook;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import com.restfb.types.Page;
import com.restfb.types.Post;
import com.restfb.types.User;

public class restFBTestMain {
    private static final String facebook_access_token = "";
    
    static FacebookClient facebookClient = new DefaultFacebookClient(facebook_access_token);
    
    /**
     * @param args
     * @throws NoSuchAlgorithmException 
     * @throws UnsupportedEncodingException 
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // TODO Auto-generated method stub
    }
    
    /**
     * 
     * @param facebookId
     * @param bdayStartTimeStamp
     * @param postLimit
     */
    public static void bdayAutoReply(String facebookId, long bdayStartTimeStamp, int postLimit) {
        String timeStampStr = Long.toString(bdayStartTimeStamp);
        // Get posts by others (max posts == 300) with time stamp >  bdayStartTimeStamp
        String fqlQuery = "SELECT post_id, permalink, message, created_time FROM stream WHERE source_id = "
                + facebookId + " AND actor_id != " + facebookId
                + " AND created_time > " + timeStampStr + " LIMIT 300";
        List<FacebookPost> facebookPostsList = facebookClient.executeFqlQuery(fqlQuery, FacebookPost.class);

        for (FacebookPost post : facebookPostsList) {
            String postId = post.post_id;
            String comment = getBdayPostReplyComment();
            facebookClient.publish(postId + "/likes", Boolean.class);
            facebookClient.publish(postId + "/comments", FacebookType.class, Parameter.with("message", comment));
            System.out.println(postId + " Comment published " + comment);

        }
    }
    
    public static String getBdayPostReplyComment() {
        String[] s = {"Thanks :)", "Thanks a lot :)", "Thanku :)"};
        Random random = new Random();
        int index = Math.abs(random.nextInt()) % 3;
        return s[index];
    }
    
    public static void getPagePosts(long pageId) {
        String pageIdStr = Long.toString(pageId);
        
        String fqlQuery = "SELECT permalink FROM stream WHERE source_id = " + pageIdStr + " AND actor_id = " + pageIdStr 
                + " LIMIT " + 1;
        List<FacebookPost> facebookPostsList = facebookClient.executeFqlQuery(fqlQuery, FacebookPost.class);
        System.out.println(facebookPostsList.get(0).permalink);
    }
    
    class fbUser {
        public String name;
        public String id;
    }
    
    /**
     * Like comments by users in userIdList on page with id in pageIdList
     * @param userIDList
     * @param pageIdList
     */
    public static void restFBCommentLikeTest(ArrayList<String> userIdList, ArrayList<String> pageIdList) {
        for (String idPage : pageIdList) {
            System.out.println("Processing PageID : " + idPage);
            String queryPosts = "SELECT post_id from stream where source_id=" + idPage + " AND actor_id=" + idPage
                    + " LIMIT 100";

            List<String> fqlPosts = facebookClient.executeFqlQuery(queryPosts, String.class);

            String queryParam = "";
            boolean z = false;

            for (String post : fqlPosts) {
                if (z) {
                    queryParam += ", ";
                }

                z = true;

                String postId = post.substring(post.lastIndexOf("_") + 1, post.lastIndexOf("\""));
                queryParam += postId;
            }

            for (String idUser : userIdList) {
                System.out.println("Processing UserID : " + idUser);

                String queryComments = "SELECT id from comment where post_id IN (" + queryParam + ") AND fromid= "
                        + idUser + " LIMIT 5000";
                List<String> fqlComments = facebookClient.executeFqlQuery(queryComments, String.class);

                for (String comment : fqlComments) {
                    String id = comment.substring(7, comment.lastIndexOf("\""));
                    System.out.println("Processing comment : " + id);

                    try {
                        facebookClient.deleteObject(id + "/likes");
                        System.out.println("Comment has already been liked");
                    } catch (Exception e) {
                        System.out.println("Comment has not yet been liked");
                        facebookClient.publish(id + "/likes", Boolean.class);
                    }
                }
            }
        }
    }
    
    /**
     * List posts from page (default Limit)
     * @param pageID
     */
    public static void restFBPageTest(String pageID) {
        String query = "SELECT post_id FROM stream WHERE source_id = " + pageID + " AND actor_id = " + pageID;
        List<String> fqlPosts = facebookClient.executeFqlQuery(query, String.class);
        for (String pagePost : fqlPosts) {            
            System.out.println(pagePost);
        }
    }
    
    /**
     * Group operations
     */
    public static void restFBGroupTest(String groupID) {
        Post post = facebookClient.fetchObject(groupID, Post.class);
       System.out.println(post.getLikesCount());
    }
    
    // Holds results from an "executeFqlQuery" call.
    // You need to write this class yourself!
    // Be aware that FQL fields don't always map to Graph API Object fields.

    public class FqlUser {
      String uid;
      
      String name;

      @Override
      public String toString() {
        return String.format("%s (%s)", name, uid);
      }
    }
    public static void restFBTest(String UserID, String pageID) {
        User user = facebookClient.fetchObject(UserID, User.class);
        Page page = facebookClient.fetchObject(pageID, Page.class);
        
        System.out.println("User name: " + user.getUsername());
        System.out.println("Page likes: " + page.getLikes());
    }
    
    public static String encrypt(String s) {
        String encryptedString = "";
            
        for (int i=0; i < s.length(); i++) {
            encryptedString += (char)(s.charAt(i)-(i%5+1));
        }
        
        encryptedString = encryptedString.replaceAll("&", "#");
        encryptedString = encryptedString.replaceAll("\"", "!");
        encryptedString = encryptedString.replaceAll("<", "'");
        encryptedString = encryptedString.replaceAll(">", "%");
        
        return encryptedString;
    }
    
    public static String decrypt(String s) {
        s = s.replaceAll("#", "&");
        s = s.replaceAll("!", "\"");
        s = s.replaceAll("'", "<");
        s = s.replaceAll("%", ">");
        
        String decryptedString = "";
        
        for (int i=0; i < s.length(); i++) {
            decryptedString += (char)(s.charAt(i)+(i%5+1));      
        }
        
        return decryptedString;
    }
}

class FacebookPost implements Comparable<FacebookPost> {
    @Facebook
    String post_id;
    
    @Facebook
    String permalink;

    @Facebook
    String message;

    @Facebook
    long created_time;
    
    public int compareTo(FacebookPost post) {
        return this.permalink.compareTo(post.permalink);
    }
}

class FqlPost {
    @Facebook
    String post_id;

    @Facebook
    long created_time;
}