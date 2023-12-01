package br.ufrn.imd.player.models;

public class UserFree extends User{
    public UserFree(String username, String senha, boolean isVIP, int id, String userIcon, String nomeCompleto, String email) {
        super(username, senha, isVIP, id, userIcon, nomeCompleto, email);
    }
}
