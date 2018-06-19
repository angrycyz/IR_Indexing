import java.util.*;

public class Similarity {
    public double calcJaccardCoef(String s1, String s2) {
        HashSet<Character> set1 = new HashSet<>();
        HashSet<Character> set2 = new HashSet<>();
        for (int i = 0; i < s1.length(); i ++) {
            set1.add(s1.charAt(i));
        }
        for (int i = 0; i < s2.length(); i ++) {
            set2.add(s2.charAt(i));
        }
        HashSet<Character> union = new HashSet<>(set1);
        union.addAll(set2);
        set1.retainAll(set2);

        return (double)set1.size()/(double)union.size();
    }

    public double calcLevinsteinDist(String s1, String s2) {
        int n1 = s1.length();
        int n2 = s2.length();

        int[][] dp = new int[n1 + 1][n2 + 1];

        for(int i = 0; i <= n1; i++)
            dp[i][0] = i;

        for(int i = 1; i <= n2; i++)
            dp[0][i] = i;

        for(int i = 0; i < n1; i++) {
            for(int j = 0; j < n2; j++) {
                if(s1.charAt(i) == s2.charAt(j))
                    dp[i + 1][j + 1] = dp[i][j];
                else {
                    dp[i + 1][j + 1] = Collections.min(Arrays.asList(dp[i][j], dp[i][j + 1], dp[i + 1][j]));
                    dp[i + 1][j + 1]++;
                }
            }
        }
        return dp[n1][n2];
    }

    public static void main(String[] args) {
        Similarity similarity = new Similarity();
        System.out.println(similarity.calcJaccardCoef("dog", "cog"));
    }
}
