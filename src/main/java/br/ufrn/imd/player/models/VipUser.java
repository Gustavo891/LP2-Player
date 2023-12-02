package br.ufrn.imd.player.models;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VipUser extends User{

    /**
     * Construtor do usuário VIP.
     * @param username
     * @param senha
     * @param isVIP
     * @param id
     * @param userIcon
     * @param nomeCompleto
     * @param email
     */
    public VipUser(String username, String senha, boolean isVIP, int id, String userIcon, String nomeCompleto, String email) {
        super(username, senha, isVIP, id, userIcon, nomeCompleto, email);
    }
    /**
     * Recebe a informação do painel de criação da playlist, verifica se o nome é válido e cria o arquivo.
     * @param field
     * @return
     */
    public boolean criarPlaylist(TextField field) {
        String playlistName = field.getText();
        if (isValidPlaylistName(playlistName) && !(playlistName == "null")) {
            String playlistFileName = "playlists" + File.separator + getId() + "_" + playlistName + ".txt";
            try (PrintWriter writer = new PrintWriter(playlistFileName)) {
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("Erro ao salvar a playlist no arquivo: " + playlistFileName);
                return false;
            }
        } else {
            return false;
        }
    }
    /**
     * Recebe o nome da playlist, e deleta ela caso exista.
     * @param playlistName
     */
    public void deletePlaylist(String playlistName) {
        Path playlistFilePath = Paths.get("playlists", getId() + "_" + playlistName + ".txt");

        try {
            Files.deleteIfExists(playlistFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao excluir a playlist: " + playlistFilePath);
        }
    }
    /**
     * Recebe o caminho da música e adiciona ela no arquivo da playlist.
     * @param selectedPlaylist
     * @param musicPath
     * @param playlistMusic
     */
    public void addMusicToPlaylist(String selectedPlaylist, String musicPath, ListView playlistMusic) {
        if (selectedPlaylist != null) {
            Path playlistFilePath = Paths.get("playlists", getId() + "_" + selectedPlaylist + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(String.valueOf(playlistFilePath), true))) {
                writer.write(musicPath);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Não foi possível adicionar a música à playlist.");
            }
            loadPlaylistSongs(selectedPlaylist, playlistMusic);
            playlistMusic.refresh();
        }
    }
    /**
     * Carrega as músicas do arquivo da playlist.
     * @param playlistName
     * @param playlistMusic
     */
    public void loadPlaylistSongs(String playlistName, ListView playlistMusic) {
        Path playlistFilePath = Paths.get("playlists", getId() + "_" + playlistName + ".txt");

        // Verifica se o arquivo existe
        if (!Files.exists(playlistFilePath)) {
            try {
                Files.createFile(playlistFilePath); // Isso criará o arquivo se ele não existir
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Erro ao criar o arquivo da playlist: " + playlistFilePath);
                return; // Se houver um erro ao criar o arquivo, encerramos aqui para evitar mais problemas
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(playlistFilePath)))) {
            playlistMusic.getItems().clear();

            String line;
            while ((line = reader.readLine()) != null) {
                String musicName = extractMusicName(line);
                playlistMusic.getItems().add(musicName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao ler o arquivo da playlist: " + playlistFilePath);
        }
    }
    /**
     * Extrai o nome da música do arquivo da playlist.
     * @param filePath
     * @return
     */
    public String extractMusicName(String filePath) {
        int lastSeparatorIndex = filePath.lastIndexOf(File.separator);
        int extensionIndex = filePath.lastIndexOf(".mp3");

        if (lastSeparatorIndex >= 0) {
            return filePath.substring(lastSeparatorIndex + 1, extensionIndex);
        } else {
            return filePath;
        }
    }
    /**
     * Extrai o nome da playlist. De "1_teste.txt" para "teste"
     * @param fileName
     * @return
     */
    public String extractPlaylistName(String fileName) {
        int underscoreIndex = fileName.indexOf('_');
        int dotIndex = fileName.lastIndexOf('.');

        if (underscoreIndex >= 0 && dotIndex > underscoreIndex) {
            return fileName.substring(underscoreIndex + 1, dotIndex);
        } else {
            System.out.println("Erro ao extrair o nome da playlist do arquivo: " + fileName);
            return null;
        }
    }
    /**
     * Verifica se o nome da playlist é válido.
     * @param playlistName
     * @return
     */
    private boolean isValidPlaylistName(String playlistName) {
        return !playlistName.trim().isEmpty();
    }
    /**
     * Retorna o caminho da playlist.
     * @param playlistName
     * @param musicName
     * @return
     */
    public String getPlaylistMusicPath(String playlistName, String musicName) {
        String playlistFilePath = "playlists" + File.separator + getId() + "_" + playlistName + ".txt"; // Adicione a extensão do arquivo

        try (BufferedReader reader = new BufferedReader(new FileReader(playlistFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String currentMusicName = extractMusicName(line);
                if (currentMusicName.equals(musicName)) {
                    return line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    /**
     * Carrega as playlists do usuário VIP.
     * @param playlistsView
     */
    public void loadPlaylists(MFXComboBox playlistsView) {
        File playlistFolder = new File("playlists");
        File[] playlistFiles = playlistFolder.listFiles();

        if (playlistFiles != null) {
            playlistsView.getItems().clear();

            Arrays.stream(playlistFiles)
                    .filter(File::isFile)
                    .filter(file -> {
                        String playlistName = extractPlaylistName(file.getName());
                        return playlistName != null && !playlistName.equals("null") && file.getName().startsWith(getId() + "_");
                    })
                    .forEach(file -> {
                        String playlistName = extractPlaylistName(file.getName());
                        playlistsView.getItems().add(playlistName);
                    });
        }
    }
    /**
     * Edita a playlist do nome antigo para o novo caso esteja disponível.
     * @param playlistNameAtual
     * @param novoNome
     * @return
     */
    public boolean editarPlaylist(String playlistNameAtual, String novoNome) {
        if (isValidPlaylistName(novoNome) && !novoNome.equals("null")) {
            if (novoNome.isEmpty()) {
                System.out.println("O novo nome da playlist não pode ser uma string vazia.");
                return false;
            }

            Path playlistFilePathAtual = Paths.get("playlists", getId() + "_" + playlistNameAtual + ".txt");
            Path playlistFilePathNovo = Paths.get("playlists", getId() + "_" + novoNome + ".txt");

            System.out.println(playlistNameAtual + " & " + novoNome);

            if (Files.exists(playlistFilePathAtual)) {
                try {
                    Files.move(playlistFilePathAtual, playlistFilePathNovo);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Erro ao editar a playlist: " + playlistNameAtual);
                    System.out.println("Erro 2");
                    return false;
                }
            } else {
                System.out.println("Arquivo não encontrado: " + playlistFilePathAtual);
                System.out.println("Erro 3");
                return false;
            }
        } else {
            System.out.println("Erro 4");
            return false;
        }
    }
    /**
     * Remove a música de uma playlist.
     * @param playlistName
     * @param musicName
     * @param playlistMusic
     */
    public void removerMusica(String playlistName, String musicName, ListView playlistMusic) {
        Path playlistFilePath = Paths.get("playlists", getId() + "_" + playlistName + ".txt");

        try {
            List<String> lines = Files.readAllLines(playlistFilePath);

            // Filtra as linhas para remover a música específica
            List<String> updatedLines = lines.stream()
                    .filter(line -> !extractMusicName(line).equals(musicName))
                    .collect(Collectors.toList());

            // Reescreve o arquivo com as linhas atualizadas
            Files.write(playlistFilePath, updatedLines);

            // Recarrega as músicas da playlist na ListView
            loadPlaylistSongs(playlistName, playlistMusic);

            System.out.println("Música removida com sucesso: " + musicName);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao remover a música da playlist: " + playlistFilePath);
        }
    }
}
