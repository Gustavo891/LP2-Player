package br.ufrn.imd.player.ControlMain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
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

    public void setLogado(UserFree usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        System.out.println("Logado ID" + usuarioLogado.getId());
        userDirectories.setUserId(usuarioLogado.getId());
        initialize();
    }

    protected Boolean todas = false;
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
    protected FontAwesomeIcon botaoPlay, botaoPausar;
    @FXML
    protected ImageView imagemPerfil, imagemPerfil1;

    protected List<String> directories = new ArrayList<>();
    protected MediaPlayer mediaPlayer;
    public void initialize() {
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
            // Se o popup do ComboBox estiver aberto, oculta o ListView
            if (newValue) {
                loadDirectoriesFromConfig();
                loadAllMusicas();
            }
            musicListView.setVisible(!newValue);
        });
        testeDiretorio.getSelectionModel().selectFirst();
    }

    protected void loadPerfil() {
        String icon = usuarioLogado.getUserIcon();
        try {
            if(usuarioLogado.getUserIcon().equals("padrao")) {
                System.out.println("imagem padrão");
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/perfil.jpg")));
                imagemPerfil.setImage(image);
                imagemPerfil1.setImage(image);

            } else {
                System.out.println("imagem custom");
                Image image = new Image("file:" + icon);
                imagemPerfil.setImage(image);
                imagemPerfil1.setImage(image);
                System.out.println(usuarioLogado.getUserIcon());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao carregar a imagem: " + e.getMessage());
        }
        perfilEmail.setText(usuarioLogado.getEmail());
        perfilNome.setText(usuarioLogado.getUsername());
    }
    @FXML
    protected void loadDirectoriesFromConfig() {
        testeDiretorio.getItems().clear();
        directories = userDirectories.loadDirectoriesFromConfig();
        if (directories != null) {
            testeDiretorio.getItems().addAll(directories.stream().map(dir -> new File(dir).getName()).toList());


        }

    }
    protected void clearlistViews() {
        musicListView.getItems().clear();
        testeDiretorio.getItems().clear();
    }

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
    @FXML
    protected boolean isMusicPlaying() {
        return mediaPlayer != null &&
                (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING ||
                        mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED);
    }
    @FXML
    protected void clicouMusica2() {
        todas = false;
        String selectedMusic = musicListView.getSelectionModel().getSelectedItem();
        if (selectedMusic != null) {
            playSelectedMusic();
        }
    }

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
    @FXML
    protected void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
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
    @FXML
    protected String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
    protected List<UserFree> readUsersFromFile() {
        File file = new File("registros.json");

        if (!file.exists()) {
            // Se o arquivo não existe, cria um arquivo vazio
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
    protected void writeUsersToFile(List<UserFree> users) {
        File file = new File("registros.json");

        try {
            // Cria o arquivo se não existir
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
    @FXML
    protected TextField regNome, regEmail, regSenha;
    @FXML
    protected Button botaoFoto;
    @FXML
    protected Pane painelPerfil, painelConfig;
    protected String caminhoFoto;
    protected Boolean foto = false;

    public void voltarConfig(MouseEvent mouseEvent) {
        painelPerfil.setVisible(true);
        painelConfig.setVisible(false);
    }

    public void abrirConfig(MouseEvent mouseEvent) {
        regEmail.setPromptText(usuarioLogado.getEmail());
        regNome.setPromptText(usuarioLogado.getNomeCompleto());
        regSenha.setPromptText(usuarioLogado.getSenha());
        painelConfig.setVisible(true);
        painelPerfil.setVisible(false);
    }

    @FXML
    protected void salvarConfigRegistro(MouseEvent mouseEvent) {
        String novoNome = regNome.getText();
        String novoEmail = regEmail.getText();
        String novaSenha = regSenha.getText();

        if (!novoNome.isEmpty() || !novoEmail.isEmpty() || !novaSenha.isEmpty() || foto) {
            List<UserFree> users = readUsersFromFile();

            // Find the logged-in user in the list
            for (UserFree user : users) {
                if (user.getId() == usuarioLogado.getId()) {
                    // Update user information if fields are not empty
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
                    break; // Stop searching once the user is found and updated
                }
                System.out.println("USUARIO NOVO: " + user);
            }

            writeUsersToFile(users);
            loadPerfil();
            System.out.println("O usuário foi atualizado.");
        } else {
            System.out.println("Não teve nada para ser atualizado.");
        }
    }

    public void escolherFoto(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolher Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Todos os Arquivos", "*.*")
        );

        // Abre o diálogo de escolha de arquivo
        File selectedFile = fileChooser.showOpenDialog(null);
        System.out.println(selectedFile);
        // Verifica se um arquivo foi selecionado
        if (selectedFile != null) {
            caminhoFoto = selectedFile.getAbsolutePath();
            System.out.println(caminhoFoto);
            Image image = new Image("file:" + caminhoFoto);
            imagemPerfil1.setImage(image);
            foto = true;
        } else {
            foto = false;
            System.out.println("Nenhum arquivo selecionado.");
        }
    }

    @FXML
    protected void loadAllMusicas() {
        // Limpa a ListView antes de carregar as músicas
        musicasTotais.getItems().clear();

        // Abre o arquivo musicas.txt para escrita
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("musicas.txt"))) {
            // Percorre todos os diretórios do usuário
            for (String directory : directories) {
                File selectedDirectory = new File(directory);
                File[] mp3Files = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

                if (mp3Files != null) {
                    for (File mp3File : mp3Files) {
                        String musicPath = mp3File.getAbsolutePath();
                        String musicName = removeFileExtension(mp3File.getName());

                        // Verifica se o caminho da música existe
                        if (new File(musicPath).exists()) {
                            // Escreve o caminho da música no arquivo musicas.txt
                            writer.write(musicPath);
                            writer.newLine();

                            // Adiciona o nome da música na ListView musicasTotais
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


    @FXML
    protected void clicouTodas() {
        todas = true;
        String selectedMusicName = musicasTotais.getSelectionModel().getSelectedItem();

        if (selectedMusicName != null) {
            // Read the musicas.txt file to get the path of the selected music
            String musicFilePath = getMusicFilePath(selectedMusicName);

            if (musicFilePath != null) {
                // Stop any currently playing music
                stopMusic();

                // Play the selected music
                playSelectedMusicFile(musicFilePath, selectedMusicName);
            } else {
                System.out.println("Caminho da música não encontrado.");
            }
        }
    }
    protected String getMusicFilePath(String musicName) {
        // Read the musicas.txt file to find the path of the given music name
        try (BufferedReader reader = new BufferedReader(new FileReader("musicas.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Check if the line contains the music name
                if (line.contains(musicName)) {
                    return line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao ler o arquivo musicas.txt");
        }
        return null; // Music name not found
    }


    protected void playSelectedMusicFile(String musicFilePath, String selectedMusicName) {
        // Stop any currently playing music
        stopMusic();

        // Play the selected music
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
