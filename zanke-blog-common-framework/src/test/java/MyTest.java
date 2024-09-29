import com.fasterxml.jackson.databind.ObjectMapper;
import com.zanke.pojo.entity.Article;
import com.zanke.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public class MyTest {

    public static void main(String[] args) {

        System.out.println(1L << 62);

        String token = JwtUtil.createJWT("123", 3000L);

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean flag = bCryptPasswordEncoder.upgradeEncoding("$2a$10$Jnq31rRkNV3RNzXe0REsEOSKaYK8UgVZZqlNlNXqn");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Claims claims = JwtUtil.parseJWT(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<String> strings = new ArrayList<>();
        strings.add("1");
        strings.add("2");

        String json = objectMapper.writeValueAsString(strings);

        System.out.println(json);

//        ArrayList<String> strings1 = objectMapper.<ArrayList<String>>readValue(json, String.class);
    }

    @Test
    public void test2() {

        Set<ZSetOperations.TypedTuple<Article>> collect;
        HashSet<Article> articleSet = new HashSet<>();
        collect = articleSet.stream()
                .map(article -> new DefaultTypedTuple<>(article, (double) article.getViewCount()))
                .collect(Collectors.toSet());

        System.out.println(collect);
    }

    @Test
    public void test3() {
        long start = System.currentTimeMillis();

        for (int i = 1; i <= 1000 ; i++) {

            ArrayList<Integer> integers = new ArrayList<>();
            for (int j = 1; j <= 1000; j++) {
                if ((i * j) % 2 == 0) {
                    integers.add(i * j);
                }
            }
        }

        long end = System.currentTimeMillis();

        System.out.println(end - start);

        System.out.println();
    }
}
