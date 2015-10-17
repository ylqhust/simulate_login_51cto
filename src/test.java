import java.io.IOException;

/**
 * Created by apple on 15/10/17.
 */
public class test {
    public static void main(String[] args) throws IOException {
        Login51CTO login = new Login51CTO("username","password");
        login.Login();
        System.out.println("签到:"+login.ToSign());
        System.out.println("信息:"+login.GetMessageCount());
    }
}
