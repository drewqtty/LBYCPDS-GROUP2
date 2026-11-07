package lbycpds.group2.logic;

import lbycpds.group2.model.Topic;
import java.util.ArrayList;
import java.util.List;

public class InsertionSort {
    // Sort topics from lowest to highest confidence
    public static void sort(List<Topic> topics) {
        for (int i = 1; i < topics.size(); i++)
        {
            Topic key = topics.get(i);
            int j = i - 1;

            while (j >= 0 && topics.get(j).compareTo(key) > 0)
            {
                topics.set(j + 1, topics.get(j));
                j -= 1;
            }

            topics.set(j + 1, key);
        }
    }
}