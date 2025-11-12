package com.danna.rompebloques;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

// Esta clase representa la pantalla principal del juego Rompebloques.
// Aquí se dibujan los elementos, se controla la lógica y se usan los sensores del celular.
public class GameScreen implements Screen {

    // Referencia al juego principal (sirve para cambiar de pantallas, si hubiera más).
    private final Rompebloques game;

    // Cámara para ver el mundo del juego.
    private OrthographicCamera camera;

    // SpriteBatch nos permite dibujar imágenes (sprites) en pantalla.
    private SpriteBatch batch;

    // Fuente para mostrar texto (mensajes).
    private BitmapFont font;

    // --- Texturas (imágenes del juego) ---
    private Texture fondoTexture;   // Imagen de fondo
    private Texture paddleTexture;  // Imagen de la paleta
    private Texture ballTexture;    // Imagen de la pelota
    private Texture blockTexture;   // Imagen de los bloques

    // --- Datos de la paleta (la base que se mueve) ---
    private float paddleX, paddleY;            // Posición X e Y
    private float paddleWidth = 100, paddleHeight = 20; // Tamaño

    // --- Datos de la bola ---
    private float ballX, ballY;                // Posición
    private float ballRadius = 10;             // Radio (tamaño)
    private float velX = 200, velY = 200;      // Velocidad en cada dirección

    // --- Clase interna que representa un bloque ---
    private static class Block {
        float x, y, w, h;                      // Posición y tamaño del bloque
        boolean destroyed = false;             // Si fue destruido o no

        Block(float x, float y, float w, float h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }

    // Lista con todos los bloques del nivel
    private List<Block> blocks;

    // --- Estados del juego ---
    private boolean gameOver = false;  // Si el jugador perdió
    private boolean win = false;       // Si ganó
    private boolean start = false;     // Si el juego ya comenzó

    // Tamaño del área visible del juego
    private float screenWidth = 800, screenHeight = 480;

    // Constructor: se ejecuta una sola vez cuando se crea la pantalla
    public GameScreen(Rompebloques game) {
        this.game = game;

        // Configuramos la cámara y la vista del juego
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);

        // Inicializamos el "pincel" para dibujar imágenes y el texto
        batch = new SpriteBatch();
        font = new BitmapFont();

        // --- Cargamos las imágenes desde la carpeta "assets" ---
        fondoTexture = new Texture("fondo.png");
        paddleTexture = new Texture("paddle.png");
        ballTexture = new Texture("ball.png");
        blockTexture = new Texture("block.png");

        // Iniciamos o reiniciamos el juego
        resetGame();
    }

    // Este método deja todo listo para comenzar una nueva partida
    private void resetGame() {
        // Posicionamos la paleta al centro y la pelota arriba de ella
        paddleX = screenWidth / 2 - paddleWidth / 2;
        paddleY = 40;
        ballX = screenWidth / 2;
        ballY = 100;

        // Reiniciamos la velocidad de la bola
        velX = 200;
        velY = 200;

        // Creamos los bloques
        blocks = new ArrayList<>();
        float startX = 50;     // posición inicial en X
        float startY = 350;    // posición inicial en Y
        float bw = 80, bh = 30; // ancho y alto de cada bloque

        // Creamos una cuadrícula de bloques (5 filas x 8 columnas)
        for (int i = 0; i < 5; i++) { // filas
            for (int j = 0; j < 8; j++) { // columnas
                blocks.add(new Block(startX + j * (bw + 10),
                    startY + i * (bh + 10),
                    bw, bh));
            }
        }

        // Reiniciamos los estados del juego
        gameOver = false;
        win = false;
        start = false;
    }

    // Este método se ejecuta muchas veces por segundo (es el "motor del juego")
    @Override
    public void render(float delta) {
        // Limpiamos la pantalla antes de dibujar
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();

        // --- SENSOR DEL CELULAR ---
        // Leemos el acelerómetro (movimiento del celular)
        float accelX = Gdx.input.getAccelerometerY();

        // Movemos la paleta según la inclinación (invertido para Android)
        paddleX -= accelX * 7;

        // Evitamos que la paleta se salga de los bordes
        if (paddleX < 0) paddleX = 0;
        if (paddleX + paddleWidth > screenWidth)
            paddleX = screenWidth - paddleWidth;

        // --- LÓGICA DEL JUEGO ---
        if (start && !gameOver && !win) {
            // Mover la bola según su velocidad
            ballX += velX * delta;
            ballY += velY * delta;

            // Rebote en los bordes laterales
            if (ballX - ballRadius < 0 || ballX + ballRadius > screenWidth)
                velX = -velX;

            // Rebote en el techo
            if (ballY + ballRadius > screenHeight)
                velY = -velY;

            // Rebote con la paleta (la base)
            if (ballY - ballRadius < paddleY + paddleHeight &&
                ballY > paddleY &&
                ballX > paddleX &&
                ballX < paddleX + paddleWidth) {
                // Cambiamos la dirección hacia arriba
                velY = Math.abs(velY);
            }

            // Si la bola cae al fondo → pierdes
            if (ballY - ballRadius < 0)
                gameOver = true;

            // --- Colisiones con los bloques ---
            for (Block b : blocks) {
                if (!b.destroyed &&
                    ballX + ballRadius > b.x && ballX - ballRadius < b.x + b.w &&
                    ballY + ballRadius > b.y && ballY - ballRadius < b.y + b.h) {
                    b.destroyed = true; // marcamos bloque como roto
                    velY = -velY;       // rebote
                }
            }

            // Verificamos si todos los bloques fueron destruidos
            boolean allDestroyed = true;
            for (Block b : blocks) {
                if (!b.destroyed) { allDestroyed = false; break; }
            }
            if (allDestroyed) win = true;
        }

        // --- DIBUJAR EN PANTALLA ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Fondo (ocupa toda la pantalla)
        batch.draw(fondoTexture, 0, 0, screenWidth, screenHeight);

        // Paleta
        batch.draw(paddleTexture, paddleX, paddleY, paddleWidth, paddleHeight);

        // Bola
        batch.draw(ballTexture, ballX - ballRadius, ballY - ballRadius,
            ballRadius * 2, ballRadius * 2);

        // Bloques
        for (Block b : blocks) {
            if (!b.destroyed)
                batch.draw(blockTexture, b.x, b.y, b.w, b.h);
        }

        // --- MENSAJES AL JUGADOR ---
        font.setColor(Color.WHITE);

        // Mensaje de inicio
        if (!start) {
            font.draw(batch, "¡Inclina tu celular y toca para empezar!", 240, 240);
            if (Gdx.input.isTouched()) start = true; // iniciar al tocar
        }
        // Mensaje de derrota
        else if (gameOver) {
            font.setColor(Color.RED);
            font.draw(batch, "¡Perdiste! Toca para reiniciar.", 280, 240);
            if (Gdx.input.isTouched()) resetGame();
        }
        // Mensaje de victoria
        else if (win) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "¡Ganaste! Toca para jugar otra vez.", 250, 240);
            if (Gdx.input.isTouched()) resetGame();
        }

        batch.end();
    }

    // Métodos que LibGDX exige implementar (aunque no los usemos)
    @Override public void resize(int width, int height) {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    // Liberar memoria al cerrar el juego
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        fondoTexture.dispose();
        paddleTexture.dispose();
        ballTexture.dispose();
        blockTexture.dispose();
    }
}
