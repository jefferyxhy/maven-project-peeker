package com.jefferyxhy.plugins.mavenprojectpeeker.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repo {
    public String name;
    public String connection;
    public String url;
    public List<String> alias;
    public Map<String, String> artifacts;

    public Repo() {
        name = "";
        connection = "";
        url = "";
        alias = new ArrayList<>();
        artifacts = new HashMap<>();
    }
}
