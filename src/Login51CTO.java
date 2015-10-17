import org.apache.http.*;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by apple on 15/10/17.
 */
public class Login51CTO {

    private final String LOGIN_PAGE_URL = "http://home.51cto.com/index.php?s=/Index/index/t/5/";
    private final String LOGIN_URL = "http://home.51cto.com/index.php?s=/Index/doLogin";
    private final String HOME_URL = "http://home.51cto.com/index.php?s=/Home/index";
    private final String TOSIGN_URL = "http://home.51cto.com/index.php?s=/Home/toSign";
    private final String GET_MESSAGE_COUNT = "http://home.51cto.com/index.php?s=/Index/getMsgCount";
    private int uid;

    private String email;
    private String passwd;

    private CloseableHttpClient mClient = null;

    public Login51CTO(String email,String passwd)
    {
        this.email = email;
        this.passwd = passwd;
    }

    public void Login() throws IOException {
        VisitLoginPage();
        HttpPost post = FillInForm();
        HttpResponse response = ClickLoginButton(post);
        List<String> readyVisit = ResolveResult(response);
        VisitHomePage(readyVisit);
    }




    private void VisitLoginPage() throws IOException {
        getClient().execute(new HttpHost("home.51cto.com"), getRequest("GET", LOGIN_PAGE_URL));
    }

    private HttpPost FillInForm()
    {
        HttpPost post = new HttpPost(LOGIN_URL);
        post.addHeader("Referer",LOGIN_PAGE_URL);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("email",email));
        params.add(new BasicNameValuePair("passwd",passwd));
        params.add(new BasicNameValuePair("reback", ""));

        post.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
        return post;
    }

    private HttpResponse ClickLoginButton(HttpPost post) throws IOException {
        return getClient().execute(post);
    }



    private List<String> ResolveResult(HttpResponse response) throws IOException {
        final int code = response.getStatusLine().getStatusCode();
        if (code == 302)
        {
            System.out.println("Login Failed");
            return null;
        }
        if (code == 200)
        {
            System.out.println("Login Success");

            return ResolveResponse(EntityUtils.toString(response.getEntity()));
        }
        System.out.println("UNKOWN CODE:"+code);
        return null;
    }

    private List<String> ResolveResponse(String s) {
        List<String> urls = new ArrayList<String>();
        String regx = "src=\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(s);
        while(matcher.find())
        {
            urls.add(matcher.group(1));
        }
        return urls;
    }

    private void VisitHomePage(List<String> readyVisit) throws IOException {
        CloseableHttpClient client = getClient();
        for(String url : readyVisit)
        {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            System.out.println(EntityUtils.toString(response.getEntity()));
        }
        HttpGet get = new HttpGet(HOME_URL);
        HttpResponse response = getClient().execute(get);
        uid = getUid(EntityUtils.toString(response.getEntity()));
    }

    private int getUid(String s) {
        Pattern pattern = Pattern.compile("uid=([0-9]*)");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find())
            return Integer.valueOf(matcher.group(1));
        return -1;
    }


    public String ToSign() throws IOException {
        HttpPost post = new HttpPost(TOSIGN_URL);
        return EntityUtils.toString(getClient().execute(post).getEntity());
    }

    public String GetMessageCount() throws IOException {
        HttpPost post = new HttpPost(GET_MESSAGE_COUNT);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("uid",uid+""));
        post.setEntity(new UrlEncodedFormEntity(params,Consts.UTF_8));
        return EntityUtils.toString(getClient().execute(post).getEntity());
    }

    private CloseableHttpClient getClient()
    {
        if (this.mClient == null)
        {
            RequestConfig globalConfig = RequestConfig.custom().
                    setCookieSpec(CookieSpecs.STANDARD_STRICT).
                    build();
            this.mClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
        }
        return mClient;
    }



    private HttpRequest getRequest(String method,String uri)
    {
        BasicHttpRequest mRequest = new BasicHttpRequest(method,uri);
        mRequest.addHeader("Host","home.51cto.com");
        mRequest.addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0");
        mRequest.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        mRequest.addHeader("Accept-Language","en-US,en;q=0.5");
        mRequest.addHeader("Accept-Encoding","gzip, deflate");
        return mRequest;
    }

}
