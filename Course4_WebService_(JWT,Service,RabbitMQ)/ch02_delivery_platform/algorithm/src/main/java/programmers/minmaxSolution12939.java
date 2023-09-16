package programmers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Solution12939 {

    public String solution(String s) {
        String answer = "";
        List<Integer> numList = Arrays.stream(s.split(" "))
                .mapToInt(Integer::valueOf)
                .sorted()
                .boxed()
                .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append(numList.get(0));
        sb.append(" ");
        sb.append(numList.get(numList.size() - 1));

        return sb.toString();
    }
}
