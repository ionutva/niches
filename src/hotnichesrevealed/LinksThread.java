/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hotnichesrevealed;

import java.net.URLEncoder;


/**
 *
 * @author John
 */
class LinksThread extends Thread{

    int index;
    int entire_site;
    public LinksThread(int index, int entire_site) {
        this.index = index;
        this.entire_site = entire_site;
    }
 @Override
    public void run(){
        try{
            String page = "";
            if(entire_site == 1){
                 page = HotNichesRevealedView.myLinks.resultLinks.get(index).substring(0, HotNichesRevealedView.myLinks.resultLinks.get(index).indexOf("/", 10));
            }
            else{
                page = HotNichesRevealedView.myLinks.resultLinks.get(index);
            }
            
            
            if(page.startsWith("http://")){
                page = page.substring(7);
            }
            page = URLEncoder.encode(page, "UTF-8");
            
            
            
            
//            URL url = new URL("http://boss.yahooapis.com/ysearch/se_inlink/v1/" + page + "?appid=" + HotNichesRevealedView.API_KEY + "&format=json&count=0&omit_inlinks=domain&entire_site=" + entire_site);
//            URLConnection connection = url.openConnection();
//            String line;
//            StringBuilder builder = new StringBuilder();
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream()));
//            while ((line = reader.readLine()) != null) {
//                builder.append(line);
//            }
//            String response = builder.toString();
//            //System.out.println("res"+response);
//            JSONObject json = new JSONObject(response);
//            int mylinks = Integer.parseInt(json.getJSONObject("ysearchresponse").getString("totalhits"));
            
//            URL url = new URL("http://api.bing.net/json.aspx?AppId="+ SEO2View.BingAPI +"&Query=" + "inbody:\"" + page + "\"" + "&Sources=Web&Version=2.0&Market=en-us&JsonType=callback&JsonCallback=SearchCompleted");//&count=10
//            System.out.println("page:" + page);
//            URLConnection connection = url.openConnection();
//            String line;
//            StringBuilder builder = new StringBuilder();
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream()));
//            while ((line = reader.readLine()) != null) {
//                builder.append(line);
//            }
//            String response = builder.toString();
//            response = response.substring(response.indexOf("SearchCompleted(") + 16);
//            JSONObject json = new JSONObject(response);
//            JSONObject ja = json.getJSONObject("SearchResponse").getJSONObject("Web");            
//            
//            int mylinks = Integer.parseInt(ja.getString("Total"));
            
            
            int mylinks = Integer.parseInt(HotNichesRevealedApp.myView.getBingCountResult("inbody:" + page));
            if(entire_site == 0){
               HotNichesRevealedView.pageLinks[index]=mylinks;
            }else{
               HotNichesRevealedView.domainLinks[index]=mylinks;
            }
        }
        catch(Exception e){}



    }
}
