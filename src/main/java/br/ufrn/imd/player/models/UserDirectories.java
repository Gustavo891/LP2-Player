package br.ufrn.imd.player.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserDirectories {
    private int userId;
    private List<String> directories;
    protected static final String CONFIG_FILE = "config.json";


    public UserDirectories(int userId) {
        this.userId = userId;
        this.directories = new ArrayList<>();
    }

    /**
     * Retorna o id do usuário.
     * @return
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Seta o id do usuário para os diretórios.
     * @param user
     */
    public void setUserId(int user) {
        this.userId = user;
    }

    /**
     * Retorna os diretórios.
     * @return
     */
    public List<String> getDirectories() {
        return directories;
    }

    /**
     * Adiciona um diretório novo para o usuário.
     * @param directoryPath
     */
    public void addDirectory(String directoryPath) {
        directories.add(directoryPath);
    }

    /**
     * Carrega os diretórios do usuário da config.
     * @return
     */
    public List<String> loadDirectoriesFromConfig() {
        try (Reader reader = new FileReader(CONFIG_FILE)) {
            Gson gson = new Gson();
            List<UserDirectories> userDirectoriesList = gson.fromJson(reader, new TypeToken<List<UserDirectories>>() {}.getType());
            if (userDirectoriesList == null) {
                userDirectoriesList = new ArrayList<>();
            }
            int userId = getUserId();
            UserDirectories userDirectories = userDirectoriesList.stream()
                    .filter(userDirs -> userDirs.getUserId() == userId)
                    .findFirst()
                    .orElse(null);

            if (userDirectories != null) {
                List<String> directories = userDirectories.getDirectories();

                // Remover diretórios que não existem mais no sistema de arquivos
                directories.removeIf(dir -> !new File(dir).exists());

                return (directories != null && !directories.isEmpty()) ? directories : new ArrayList<>();
            }
        } catch (IOException e) {
            System.out.println("Arquivo de configuração não encontrado. Criando um novo.");
        }
        // Retorna lista vazia em caso de erro ou se directories estiver vazio.
        return new ArrayList<>();
    }
    /**
     * Salva os diretórios do usuário na config, tanto os novos, como os antigos.
     */
    public void saveDirectoriesToConfig() {
        try (Reader reader = new FileReader(CONFIG_FILE)) {
            Gson gson = new Gson();
            List<UserDirectories> userDirectoriesList = gson.fromJson(reader, new TypeToken<List<UserDirectories>>() {}.getType());
            if (userDirectoriesList == null) {
                userDirectoriesList = new ArrayList<>();
            }

            // Procura a instância existente para o usuário atual
            UserDirectories existingUserDirs = userDirectoriesList.stream()
                    .filter(userDirs -> userDirs.getUserId() == this.getUserId())
                    .findFirst()
                    .orElse(null);

            if (existingUserDirs != null) {
                // Remove todos os diretórios que não existem mais no sistema de arquivos
                existingUserDirs.getDirectories().removeIf(dir -> !directoryExists(dir));

                // Adiciona os novos diretórios à instância existente, evitando duplicatas
                for (String newDir : this.getDirectories()) {
                    // Verifica se o diretório existe antes de adicioná-lo
                    if (!existingUserDirs.getDirectories().contains(newDir) && directoryExists(newDir)) {
                        existingUserDirs.getDirectories().add(newDir);
                    }
                }

                // Atualiza o arquivo config.json
                try (Writer writer = new FileWriter(CONFIG_FILE)) {
                    gson.toJson(userDirectoriesList, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                // Adiciona a instância atual à lista
                userDirectoriesList.add(this);

                // Atualiza o arquivo config.json
                try (Writer writer = new FileWriter(CONFIG_FILE)) {
                    gson.toJson(userDirectoriesList, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Arquivo de configuração não encontrado. Criando um novo.");
            // Se o arquivo não existir, cria uma nova lista com a instância atual
            List<UserDirectories> userDirectoriesList = new ArrayList<>();
            userDirectoriesList.add(this);

            try (Writer writer = new FileWriter(CONFIG_FILE)) {
                Gson gson = new Gson();
                gson.toJson(userDirectoriesList, writer);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Verifica se um diretório existe. Retorna true caso exista.
     * @param directoryPath
     * @return
     */
    private boolean directoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.exists();
    }



}
