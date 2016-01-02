package io.github.likcoras.agar;

import lombok.Value;

import java.util.List;

@Value
public class Config {
    String nick;
    String user;
    String gecos;
    String password;
    
    String host;
    int port;
    boolean ssl;
    
    List<String> channels;
    
    String listLink;
    String nickLink;
    String googleApi;
}
