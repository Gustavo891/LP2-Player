package br.ufrn.imd.player.models;


public abstract class User {
    public int id;
    public String username;
    public String senha;
    public boolean isVIP = false;
    public String userIcon;
    public String nomeCompleto;
    public String email;

    public User(String username, String senha,boolean isVIP, int id, String userIcon, String nomeCompleto, String email)  {
        this.isVIP = isVIP;
        this.username = username;
        this.senha = senha;
        this.id = id;
        this.userIcon = userIcon;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }
    public void setNomeCompleto(String nome) {
        this.nomeCompleto = nome;
    }

    public String getUsername() {
        return username;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String getSenha() {
        return senha;
    }

    public boolean isVIP() {
        return isVIP;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setVIP(boolean VIP) {
        isVIP = VIP;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
