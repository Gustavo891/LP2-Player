package br.ufrn.imd.player.ControlMain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import br.ufrn.imd.player.models.UserDirectories;
import br.ufrn.imd.player.models.UserFree;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FreeControl {

    protected UserFree usuarioLogado = new UserFree("", "", false, 0, "", "", "");
    protected UserDirectories userDirectories = new UserDirectories(0);
    protected Boolean todas = false;
    protected List<String> directories = new ArrayList<>();
    protected MediaPlayer mediaPlayer;
    protected String caminhoFoto;
    protected Boolean foto = false;
    @FXML
    protected MFXComboBox testeDiretorio;
    @FXML
    protected HBox musicTocando, musicPause;
    @FXML
    protected Slider musicProgress2;
    @FXML
    protected Label tempoAtual, autor, tempoTotal, perfilNome, perfilEmail;
    @FXML
    protected ListView<String> musicListView, musicasTotais;
    @FXML
    protected ProgressBar musicProgress;
    @FXML
    protected ImageView botaoPlay, botaoPausar;
    @FXML
    protected ImageView imagemPerfil, imagemPerfil1;
    @FXML
    protected TextField regNome, regEmail, regSenha;
    @FXML
    protected Button botaoFoto;
    @FXML
    protected Pane painelPerfil, painelConfig;

    /**
     * Define o usuário logado e inicializa o controle.
     *
     * @param usuarioLogado O usuário logado.
     */
    public void setLogado(UserFree usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        userDirectories.setUserId(usuarioLogado.getId());
        initialize();
    }

    /**
     * Inicializa o controle.
     */
    public void initialize() {
        musicProgress.setVisible(false);
        musicProgress2.setVisible(false);
        painelConfig.setVisible(false);
        painelPerfil.setVisible(true);
        loadPerfil();
        clearlistViews();
        userDirectories.saveDirectoriesToConfig();
        loadDirectoriesFromConfig();
        testeDiretorio.setOnAction(event ->{
            updateMusicList();
            loadAllMusicas();
        });
        loadAllMusicas();
        configureProgressSlider();
        musicTocando.toBack();
        musicPause.toFront();
        testeDiretorio.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                loadDirectoriesFromConfig();
                loadAllMusicas();
            }
            musicListView.setVisible(!newValue);
        });
        testeDiretorio.getSelectionModel().selectFirst();
    }
    /**
     * Carrega as informações do perfil do usuário.
     */
    protected void loadPerfil() {
        String icon = usuarioLogado.getUserIcon();
        try {
            if(usuarioLogado.getUserIcon().equals("padrao")) {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/perfil.jpg")));
                imagemPerfil.setImage(image);
                imagemPerfil1.setImage(image);

            } else {
                Image image = new Image("file:" + icon);
                imagemPerfil.setImage(image);
                imagemPerfil1.setImage(image);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao carregar a imagem: " + e.getMessage());
        }
        perfilEmail.setText(usuarioLogado.getEmail());
        perfilNome.setText(usuarioLogado.getUsername());
    }
    /**
     * Carrega os diretórios do usuário a partir do arquivo de configuração.
     */
    @FXML
    protected void loadDirectoriesFromConfig() {
        testeDiretorio.getItems().clear();
        directories = userDirectories.loadDirectoriesFromConfig();
        if (directories != null) {
            testeDiretorio.getItems().addAll(directories.stream().map(dir -> new File(dir).getName()).toList());


        }

    }
    /**
     * Limpa as listas de músicas.
     */
    protected void clearlistViews() {
        musicListView.getItems().clear();
        testeDiretorio.getItems().clear();
    }
    /**
     * Avança para a próxima música na lista.
     */
    @FXML
    protected void proximaMusica() {
        if (!todas) {
            int selectedIndex = musicListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex < musicListView.getItems().size() - 1) {
                musicListView.getSelectionModel().selectNext();
                playSelectedMusic();
            } else {
                musicListView.getSelectionModel().selectFirst();
                playSelectedMusic();
            }
        } else {
            int selectedIndex = musicasTotais.getSelectionModel().getSelectedIndex();
            if (selectedIndex < musicasTotais.getItems().size() - 1) {
                musicasTotais.getSelectionModel().selectNext();
                clicouTodas();
            } else {
                musicasTotais.getSelectionModel().selectFirst();
                clicouTodas();
            }

        }
    }
    /**
     * Reproduz a música anterior na lista.
     */
    @FXML
    protected void musicaAnterior() {
        if(!todas) {
            int selectedIndex = musicListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex  != 0) {
                musicListView.getSelectionModel().selectPrevious();
            } else {
                musicListView.getSelectionModel().selectLast();
            }
            playSelectedMusic();
        } else {
            int index = musicasTotais.getSelectionModel().getSelectedIndex();
            if(index != 0) {
                musicasTotais.getSelectionModel().selectPrevious();
            } else {
                musicasTotais.getSelectionModel().selectLast();
            }
            clicouTodas();
        }

    }
    /**
     * Configura o controle do slider do progresso da música.
     */
    @FXML
    protected void configureProgressSlider() {
        if (mediaPlayer != null) {
            musicProgress2.getStyleClass().add("slider");
            musicProgress2.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (musicProgress2.isValueChanging()) {
                    Duration totalDuration = mediaPlayer.getTotalDuration();
                    Duration seekTime = totalDuration.multiply(newValue.doubleValue() / 100.0);
                    int minutes = (int) seekTime.toMinutes();
                    int seconds = (int) (seekTime.toSeconds() % 60);
                    String tempo = String.format("%02d:%02d", minutes, seconds);
                    tempoAtual.setText(tempo);
                    mediaPlayer.seek(seekTime);
                }
            });

            musicProgress2.setOnMouseClicked(event -> {
                if (!musicProgress2.isValueChanging()) {
                    double mouseX = event.getX();
                    double sliderWidth = musicProgress2.getWidth();
                    double newValue = (mouseX / sliderWidth) * 100.0;
                    musicProgress2.setValue(newValue);

                    Duration totalDuration = mediaPlayer.getTotalDuration();
                    Duration seekTime = totalDuration.multiply(newValue / 100.0);
                    int minutes = (int) seekTime.toMinutes();
                    int seconds = (int) (seekTime.toSeconds() % 60);
                    String tempo = String.format("%02d:%02d", minutes, seconds);
                    tempoAtual.setText(tempo);
                    mediaPlayer.seek(seekTime);
                }
            });
            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!musicProgress2.isValueChanging()) {
                    Duration totalDuration = mediaPlayer.getTotalDuration();
                    double progress = newValue.toMillis() / totalDuration.toMillis() * 100.0;
                    musicProgress2.setValue(progress);
                }
            });
        }
    }
    /**
     * Manipula eventos do botão de reprodução/pausa da música.
     *
     * @param mouseEvent O evento de clique do mouse.
     */
    @FXML
    protected void botaoPlayer(MouseEvent mouseEvent) {
        Object source = mouseEvent.getSource();
        if(source.equals(botaoPausar)) {
            mediaPlayer.pause();
            musicTocando.toBack();
            musicPause.toFront();

        } else if(source.equals(botaoPlay)) {
            if (isMusicPlaying()) {
                mediaPlayer.play();
                musicTocando.toFront();
                musicPause.toBack();
            } else {
                System.out.println("nenhuma música tocando");
            }
        }

    }
    /**
     * Verifica se alguma música está sendo reproduzida.
     *
     * @return true se uma música estiver sendo reproduzida, false caso contrário.
     */
    @FXML
    protected boolean isMusicPlaying() {
        return mediaPlayer != null &&
                (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING ||
                        mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED);
    }
    /**
     * Manipula o clique em uma música na lista de músicas do diretório atual.
     */
    @FXML
    protected void clicouMusica2() {
        todas = false;
        String selectedMusic = musicListView.getSelectionModel().getSelectedItem();
        if (selectedMusic != null) {
            playSelectedMusic();
        }
    }
    /**
     * Reproduz a música selecionada na lista de músicas do diretório atual.
     */
    @FXML
    protected void playSelectedMusic() {
        int selectedIndex = musicListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < musicListView.getItems().size()) {
            String selectedMusicName = musicListView.getItems().get(selectedIndex);
            String selectedDirectoryPath = directories.get(testeDiretorio.getSelectionModel().getSelectedIndex());
            String selectedMusicPath = selectedDirectoryPath + File.separator + selectedMusicName + ".mp3";

            stopMusic();

            playSelectedMusicFile(selectedMusicPath, selectedMusicName);
        }
    }
    /**
     * Configura a barra de progresso para uma determinada mídia.
     *
     * @param media A mídia a ser configurada.
     */
    @FXML
    protected void setupProgressBar(Media media) {
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double progress = newValue.toSeconds() / media.getDuration().toSeconds();
            musicProgress.setProgress(progress);
            int minutes = (int) newValue.toMinutes();
            int seconds = (int) (newValue.toSeconds() % 60);
            String tempoAtualString = String.format("%02d:%02d", minutes, seconds);
            tempoAtual.setText(tempoAtualString);
        });
    }
    /**
     * Para a reprodução da música atual.
     */
    @FXML
    protected void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
    /**
     * Abre o seletor de diretórios para escolher um novo diretório de músicas.
     */
    @FXML
    protected void chooseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Escolher diretório de músicas");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            userDirectories.addDirectory(selectedDirectory.getAbsolutePath());
            userDirectories.saveDirectoriesToConfig();
            clearlistViews();
            loadDirectoriesFromConfig();
            testeDiretorio.getSelectionModel().selectIndex(userDirectories.getDirectories().size() - 1);
            updateMusicList();
            loadAllMusicas();
        }
    }
    /**
     * Atualiza a lista de músicas com base no diretório selecionado.
     */
    @FXML
    protected void updateMusicList() {

        musicListView.getItems().clear();
        int selectedIndex = testeDiretorio.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < directories.size()) {
            String selectedDirectoryPath = directories.get(selectedIndex);
            File selectedDirectory = new File(selectedDirectoryPath);
            File[] mp3Files = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
            if (mp3Files != null) {
                for (File mp3File : mp3Files) {
                    musicListView.getItems().add(removeFileExtension(mp3File.getName()));
                }
            }
        }
    }
    /**
     * Remove a extensão de arquivo de um nome de arquivo.
     *
     * @param fileName O nome do arquivo.
     * @return O nome do arquivo sem a extensão.
     */
    @FXML
    protected String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
    /**
     * Lê os usuários do arquivo JSON e retorna a lista de usuários.
     *
     * @return A lista de usuários lida do arquivo JSON.
     */
    protected List<UserFree> readUsersFromFile() {
        File file = new File("registros.json");

        if (!file.exists()) {
            writeUsersToFile(new ArrayList<>());
        }

        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type userListType = new TypeToken<List<UserFree>>() {}.getType();
            List<UserFree> users = gson.fromJson(reader, userListType);

            if (users == null) {
                System.out.println("Arquivo existe, mas está vazio.");
                return new ArrayList<>();
            }

            return users;
        } catch (IOException e) {
            System.out.println("Erro ao tentar ler o arquivo.");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    /**
     * Escreve a lista de usuários no arquivo JSON.
     *
     * @param users A lista de usuários a ser escrita.
     */
    protected void writeUsersToFile(List<UserFree> users) {
        File file = new File("registros.json");

        try {

            if (!file.exists()) {
                file.createNewFile();
            }

            try (Writer writer = new FileWriter(file)) {
                Gson gson = new Gson();
                gson.toJson(users, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Volta para o painel de perfil a partir do painel de configurações.
     *
     * @param mouseEvent O evento de clique do mouse.
     */
    public void voltarConfig(MouseEvent mouseEvent) {
        painelPerfil.setVisible(true);
        painelConfig.setVisible(false);
    }
    /**
     * Abre o painel de configurações a partir do painel de perfil.
     *
     * @param mouseEvent O evento de clique do mouse.
     */
    public void abrirConfig(MouseEvent mouseEvent) {
        regEmail.setPromptText(usuarioLogado.getEmail());
        regNome.setPromptText(usuarioLogado.getNomeCompleto());
        regSenha.setPromptText(usuarioLogado.getSenha());
        painelConfig.setVisible(true);
        painelPerfil.setVisible(false);
    }
    /**
     * Salva as configurações do usuário após edição no painel de configurações.
     *
     * @param mouseEvent O evento de clique do mouse.
     */
    @FXML
    protected void salvarConfigRegistro(MouseEvent mouseEvent) {
        String novoNome = regNome.getText();
        String novoEmail = regEmail.getText();
        String novaSenha = regSenha.getText();

        if (!novoNome.isEmpty() || !novoEmail.isEmpty() || !novaSenha.isEmpty() || foto) {
            List<UserFree> users = readUsersFromFile();


            for (UserFree user : users) {
                if (user.getId() == usuarioLogado.getId()) {

                    if (!novoNome.isEmpty()) {
                        user.setNomeCompleto(novoNome);
                        usuarioLogado.setUsername(novoNome);
                        regNome.setText("");
                    }
                    if (!novoEmail.isEmpty()) {
                        user.setEmail(novoEmail);
                        usuarioLogado.setEmail(novoEmail);
                        regEmail.setText("");
                    }
                    if (!novaSenha.isEmpty()) {
                        user.setSenha(novaSenha);
                        usuarioLogado.setSenha(novaSenha);
                        regSenha.setText("");
                    }
                    if(foto) {
                        user.setUserIcon(caminhoFoto);
                        usuarioLogado.setUserIcon(caminhoFoto);
                    } else {
                        user.setUserIcon(usuarioLogado.getUserIcon());
                    }
                    painelPerfil.setVisible(true);
                    painelConfig.setVisible(false);
                    break;
                }
            }

            writeUsersToFile(users);
            loadPerfil();
            System.out.println("O usuário foi atualizado.");
        } else {
            System.out.println("Não teve nada para ser atualizado.");
        }
    }
    /**
     * Abre o seletor de arquivos para escolher uma nova foto de perfil.
     *
     * @param actionEvent O evento de ação.
     */
    public void escolherFoto(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolher Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Todos os Arquivos", "*.*")
        );


        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            caminhoFoto = selectedFile.getAbsolutePath();
            Image image = new Image("file:" + caminhoFoto);
            imagemPerfil1.setImage(image);
            foto = true;
        } else {
            foto = false;
            System.out.println("Nenhum arquivo selecionado.");
        }
    }
    /**
     * Carrega todas as músicas de todos os diretórios.
     */
    @FXML
    protected void loadAllMusicas() {

        musicasTotais.getItems().clear();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("musicas.txt"))) {
            for (String directory : directories) {
                File selectedDirectory = new File(directory);
                File[] mp3Files = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

                if (mp3Files != null) {
                    for (File mp3File : mp3Files) {
                        String musicPath = mp3File.getAbsolutePath();
                        String musicName = removeFileExtension(mp3File.getName());

                        if (new File(musicPath).exists()) {
                            writer.write(musicPath);
                            writer.newLine();

                            musicasTotais.getItems().add(musicName);
                        } else {
                            System.out.println("Caminho não encontrado: " + musicPath);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao escrever no arquivo musicas.txt");
        }
    }
    /**
     * Manipula o clique em uma música na lista de todas as músicas.
     */
    @FXML
    protected void clicouTodas() {
        todas = true;
        String selectedMusicName = musicasTotais.getSelectionModel().getSelectedItem();

        if (selectedMusicName != null) {
            String musicFilePath = getMusicFilePath(selectedMusicName);

            if (musicFilePath != null) {
                stopMusic();

                playSelectedMusicFile(musicFilePath, selectedMusicName);
            } else {
                System.out.println("Caminho da música não encontrado.");
            }
        }
    }
    /**
     * Obtém o caminho do arquivo de uma música pelo nome da música.
     *
     * @param musicName O nome da música.
     * @return O caminho do arquivo da música.
     */
    protected String getMusicFilePath(String musicName) {
        try (BufferedReader reader = new BufferedReader(new FileReader("musicas.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(musicName)) {
                    return line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao ler o arquivo musicas.txt");
        }
        return null;
    }
    /**
     * Reproduz a música selecionada a partir do caminho do arquivo e nome da música.
     *
     * @param musicFilePath O caminho do arquivo da música.
     * @param selectedMusicName O nome da música.
     */
    protected void playSelectedMusicFile(String musicFilePath, String selectedMusicName) {
        musicProgress.setVisible(true);
        musicProgress2.setVisible(true);
        stopMusic();

        try {
            Media media = new Media(new File(musicFilePath).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            autor.setText(selectedMusicName);

            mediaPlayer.setOnReady(() -> {
                Duration totalDuration = media.getDuration();
                int totalMinutes = (int) totalDuration.toMinutes();
                int totalSeconds = (int) (totalDuration.toSeconds() % 60);
                String tempoTotalString = String.format("%02d:%02d", totalMinutes, totalSeconds);
                tempoTotal.setText(tempoTotalString);
            });

            configureProgressSlider();
            setupProgressBar(media);

            mediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> {
                mediaPlayer.stop();
                mediaPlayer.seek(Duration.ZERO);
                musicProgress2.setValue(0);
                musicProgress.setProgress(0);
                tempoAtual.setText("");
                autor.setText("");
                mediaPlayer.pause();
                musicTocando.toBack();
                musicPause.toFront();
                proximaMusica();
            }));

            mediaPlayer.play();
            musicPause.toBack();
            musicTocando.toFront();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Não foi possível reproduzir a música: " + musicFilePath);
        }
    }
}
