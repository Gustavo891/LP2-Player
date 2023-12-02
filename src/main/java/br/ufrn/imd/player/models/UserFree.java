package br.ufrn.imd.player.models;

public class UserFree extends User{
    /**
     * Construtor do usuário grátis.
     * @param username
     * @param senha
     * @param isVIP
     * @param id
     * @param userIcon
     * @param nomeCompleto
     * @param email
     */
    public UserFree(String username, String senha, boolean isVIP, int id, String userIcon, String nomeCompleto, String email) {
        super(username, senha, isVIP, id, userIcon, nomeCompleto, email);
    }
}
