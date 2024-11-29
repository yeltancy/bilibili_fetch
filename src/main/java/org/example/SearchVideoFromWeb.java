package org.example;

import java.util.List;

public interface SearchVideoFromWeb<T> {
    List<T> searchVideo(String sortType, String keyword, String order, int maxCount);
}
