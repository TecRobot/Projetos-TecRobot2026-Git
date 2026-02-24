package frc.robot;

import edu.wpi.first.wpilibj.Timer;

public class Autonomous {
    private Movimentos movimentos;
    private Vision vision;
    private Timer timer = new Timer(); // Corrigido para Timer do WPILib
    private Timer timerGeral = new Timer(); // Cronômetro que nunca reseta durante o auto
    private double tempoParaGirar360 = 2.0; // Ajuste este valor conforme seu robô

    // VARIÁVEIS GLOBAIS DE CONFIGURAÇÃO (IDs)
    public final int[] IDS_HUB_AZUL = {25, 26};
    public final int[] IDS_HUB_VERMELHO = {1, 2};

    // Variáveis para armazenar os dados do ciclo atual
    private double distanciaAtual;
    private int fase = 0;

    public Autonomous(Movimentos movimentos, Vision vision) {
        this.movimentos = movimentos;
        this.vision = vision;
    }

    // Método para resetar o timer e sensores ao iniciar o auto
    public void setup() {
        timerGeral.reset();
        timerGeral.start();
    
        timer.reset(); // Timer das fases
        timer.start();
        fase = 0;
    }

    // Lógica para sair do MEIO
    public void Meio() {
        // --- TRAVA MESTRE DE SEGURANÇA (20 SEGUNDOS) ---
        if (timerGeral.get() > 20.0) {
            movimentos.parar();
            return; // Sai do método e não executa mais nada abaixo
        }
        
        // Se o tempo for menor que 20s, executa a lógica normal:
        distanciaAtual = vision.getDistance();
        double posicaoAlvo = 1.2; // Distancia ideal para o robo parar
        double distanciaLancamento = 1.5; // Substituir pela distancia "XX" METROS
        int idVisto = vision.getTargetID();

        if(fase == 0){
            if(distanciaAtual < posicaoAlvo){
                movimentos.frente(0.5);
                timer.reset();
                fase = 1;
            }
            //vendo a posição de acordo com o Hub
        } else if(fase == 1){ // Corrigido: ==
            if (vision.hasTarget() && (idVisto == 25 || idVisto == 26 /*Azul */ || idVisto == 1 || idVisto == 2/*Vermelho */)) {
                if(distanciaAtual > distanciaLancamento) {
                    movimentos.tras(-0.3);
                } else if(distanciaAtual < distanciaLancamento) {
                    movimentos.frente(0.3);
                } else {
                    movimentos.parar();
                    timer.reset();
                    fase = 2;
                }
            } else {
                movimentos.frente(0.3);
            }
            //fase lançamento
        } else if(fase == 2){ // Corrigido: ==
            if (timer.get() > 5) {
                movimentos.atirar(0.8, 0.7, 0.1); // Ajustado para parâmetro de força
            } else if(timer.get() > 2){
                movimentos.esquerda(0.2, 0); 
            } else if(timer.get() > 3) {
                movimentos.frente(0.3);
                movimentos.parar();
            } else {
                movimentos.esquerda(0.3, 0);
                timer.reset();
                fase = 3; 
            }
            //Indo até o Climb  
        } else if (fase == 3){
            if(timer.get() > 5){
                movimentos.frente(0.5);
                movimentos.parar();
                timer.reset();
                fase = 4;
            }  
        } else if(fase == 4){
            double tempoLimiteGiro = tempoParaGirar360 * 2;

            if (vision.hasTarget()) {
                movimentos.parar();
                timer.reset();
                fase = 5;

            //caso ele de 2 giros e não ache
            } else if (timer.get() < tempoLimiteGiro) {
                movimentos.parar();
                if(timer.get() > 7){
                    movimentos.tras(-0.5);
                    movimentos.parar();
                }
            } 
            //Alinhando com o climb e "climbando"
        } else if(fase == 5){
            distanciaAtual = vision.getDistance();
            double distanciaClimb = 0.10; //Adicionar a distancia exata para climbar

            if(distanciaAtual > distanciaClimb){
                movimentos.tras(-0.3);
                movimentos.parar();
            }else if(distanciaClimb > distanciaAtual){
                movimentos.frente(0.3);
                movimentos.parar();
            } else {
                movimentos.parar();
                timer.reset();
                fase = 6;
            }
        } else if(fase == 6){
            movimentos.Climb(0.1);
        }
    }

    // Lógica para sair da DIREITA - Vai para frente até XX metros da Tag
    public void Direita() {
        // --- TRAVA MESTRE DE SEGURANÇA (20 SEGUNDOS) ---
        if (timerGeral.get() > 20.0) {
            movimentos.parar();
            return; // Sai do método e não executa mais nada abaixo
        }

        // Se o tempo for menor que 20s, executa a lógica normal:
        distanciaAtual = vision.getDistance();
        double posicaoAlvo = 1.2;

        if (fase == 0) {
            if (vision.hasTarget()) {
                if (distanciaAtual > posicaoAlvo) {
                    movimentos.frente(0.4);
                } else {
                    movimentos.parar();
                    timer.reset(); //Reseta o timer para a fase 1
                    fase = 1; 
                }
            } else {
                movimentos.parar();
            }
        }
        else if (fase == 1) { //Vira para a Esquerda
            if (timer.get() < 1.0) { 
                movimentos.esquerda(0, 0.5); 
            } else {
                movimentos.parar();
                timer.reset(); //Reinicia para a fase 2
                fase = 2;
            }
        }
        else if (fase == 2) { // anda até o meio da aliança
            if (timer.get() < 3.0) {
                movimentos.frente(0.5);
            } else {
                movimentos.parar();
                timer.reset(); 
                fase = 3;
            }
        }
        else if (fase == 3) {
            // Gira procurando o Hub, mas respeita o timerGeral de 20s e o timer de 2 voltas
            double tempoLimiteGiro = tempoParaGirar360 * 2;

            if (vision.hasTarget()) {
                movimentos.parar();
                timer.reset();
                fase = 4;
            } 
            else if (timer.get() < tempoLimiteGiro) {
                movimentos.direita(0.4,0.1);
            } 
            else {
                movimentos.parar();
                fase = 99;
            }
        } else if (fase == 4) {//Se alinhando com o hub
            distanciaAtual = vision.getDistance();
            double distanciaLancamento = 1.5; // Substituir pela distancia "XX" METROS
            int idVisto = vision.getTargetID();
           
            // 1. Verifica se está vendo uma tag de Hub (qualquer uma das 4)
            if (vision.hasTarget() && (idVisto == 25 || idVisto == 26 /*Azul */ || idVisto == 1 || idVisto == 2/*Vermelho */)) {
                // 2. Se a distância for maior que a necessária, ele vai para frente
                if(distanciaAtual > distanciaLancamento ) {
                    movimentos.tras(-0.3);//vai em baixa velocidade até alcançar a distancia ideal
                    movimentos.parar();
                // 3. Se estiver muito perto, ele vai para trás
                }else if (distanciaAtual < (distanciaLancamento - 0.1)) { // Margem de erro de 10cm
                    movimentos.frente(0.3);
                }
            } 
            // 4. Se estiver na distância certa
            else {
                movimentos.parar();
                timer.reset(); // Reseta para a próxima ação (lançar)
                fase = 5;      // Pula para a próxima fase do fluxograma=
            }
        // fase do lançamento
        } else if(fase == 5){
            if (timer.get() > 5) {
                movimentos.atirar(0.8,0.7,0.8);
            } else if(timer.get() > 2){
                movimentos.esquerda(0.2, 0);
            } else if(timer.get() > 3) {
                movimentos.frente(0.3);
                movimentos.parar();
            } else {
                movimentos.esquerda(0.3, 0);
                timer.reset();
                fase = 6; 
            }
            //Indo até o Climb  
        } else if (fase == 6){
            if(timer.get() > 5){
                movimentos.frente(0.5);
                movimentos.parar();
                timer.reset();
                fase = 7;
            }
        } else if(fase == 7){
            double tempoLimiteGiro = tempoParaGirar360 * 2;

            if (vision.hasTarget()) {
                movimentos.parar();
                timer.reset();
                fase = 8;

            //caso ele de 2 giros e não ache
            } else if (timer.get() < tempoLimiteGiro) {
                movimentos.parar();
                if(timer.get() > 7){
                    movimentos.tras(-0.5);
                    movimentos.parar();
                }
            } 
            //Alinhando com o climb e "climbando"
        } else if(fase == 8){
            distanciaAtual = vision.getDistance();
            double distanciaClimb = 0.10; //Adicionar a distancia exata para climbar

            if(distanciaAtual > distanciaClimb){
                movimentos.tras(-0.3);
                movimentos.parar();
            }else if(distanciaClimb > distanciaAtual){
                movimentos.frente(0.3);
                movimentos.parar();
            } else {
                movimentos.parar();
                timer.reset();
                fase = 9;
            }
        } else if(fase == 9){
            movimentos.Climb(0.1);
        }
    }

    // Lógica para sair da ESQUERDA
    public void Esquerda() {
        // --- TRAVA MESTRE DE SEGURANÇA (20 SEGUNDOS) ---
        if (timerGeral.get() > 20.0) {
            movimentos.parar();
            return; // Sai do método e não executa mais nada abaixo
        }

        // Se o tempo for menor que 20s, executa a lógica normal:
        distanciaAtual = vision.getDistance();
        double posicaoAlvo = 1.2;

        if (fase == 0) {
            if (vision.hasTarget()) {
                if (distanciaAtual > posicaoAlvo) {
                    movimentos.frente(0.4);
                } else {
                    movimentos.parar();
                    timer.reset(); //Reseta o timer para a fase 1
                    fase = 1; 
                }
            } else {
                movimentos.parar();
            }
        }
        else if (fase == 1) { //Vira para a Direita
            if (timer.get() < 1.0) { 
                movimentos.direita(0, 0.5); 
            } else {
                movimentos.parar();
                timer.reset(); //Reinicia para a fase 2
                fase = 2;
            }
        }
        else if (fase == 2) { // anda até o meio da aliança
            if (timer.get() < 3.0) {
                movimentos.frente(0.5);
            } else {
                movimentos.parar();
                timer.reset(); 
                fase = 3;
            }
        }
        else if (fase == 3) {
            // Gira procurando o Hub, mas respeita o timerGeral de 20s e o timer de 2 voltas
            double tempoLimiteGiro = tempoParaGirar360 * 2;

            if (vision.hasTarget()) {
                movimentos.parar();
                timer.reset();
                fase = 4;
            } 
            else if (timer.get() < tempoLimiteGiro) {
                movimentos.direita(0.4, 0.1);
            } 
            else {
                movimentos.parar();
                fase = 99;
            }
        } else if (fase == 4) {//Se alinhando com o hub
            distanciaAtual = vision.getDistance();
            double distanciaLancamento = 1.5; // Substituir pela distancia "XX" METROS
            int idVisto = vision.getTargetID();
           
            // 1. Verifica se está vendo uma tag de Hub (qualquer uma das 4)
            if (vision.hasTarget() && (idVisto == 25 || idVisto == 26 /*Azul */ || idVisto == 1 || idVisto == 2/*Vermelho */)) {
                // 2. Se a distância for maior que a necessária, ele vai para frente
                if(distanciaAtual > distanciaLancamento ) {
                    movimentos.tras(-0.3);//vai em baixa velocidade até alcançar a distancia ideal
                    movimentos.parar();
                // 3. Se estiver muito perto, ele vai para trás
                }else if (distanciaAtual < (distanciaLancamento - 0.1)) { // Margem de erro de 10cm
                    movimentos.frente(0.3);
                }
            } 
            // 4. Se estiver na distância certa
            else {
                movimentos.parar();
                timer.reset(); // Reseta para a próxima ação (lançar)
                fase = 5;      // Pula para a próxima fase do fluxograma=
            }
        // fase do lançamento
        } else if(fase == 5){
            if (timer.get() > 5) {
                movimentos.atirar(0.8,07,0.8); // Atira por 5s
            } else if(timer.get() > 2){
                movimentos.direita(0.2, 0); // vai para o lado para ficar na distancia do climb
            } else if(timer.get() > 3) { // vai para a direita só um pouco
                movimentos.frente(0.3);
                movimentos.parar();
            } else {
                movimentos.direita(0.3, 0); // vira para a direção do climb
                timer.reset();
                fase = 6; 
            }
            //Indo até o Climb  
        } else if (fase == 6){
            if(timer.get() > 5){
                movimentos.frente(0.5);
                movimentos.parar();
                timer.reset();
                fase = 7;
            }
        } else if(fase == 7){
            double tempoLimiteGiro = tempoParaGirar360 * 2;

            if (vision.hasTarget()) {
                movimentos.parar();
                timer.reset();
                fase = 8;

            //caso ele de 2 giros e não ache
            } else if (timer.get() < tempoLimiteGiro) {
                movimentos.parar();
                if(timer.get() > 7){
                    movimentos.tras(-0.5);
                    movimentos.parar();
                }
            } 
            //Alinhando com o climb e "climbando"
        } else if(fase == 8){
            distanciaAtual = vision.getDistance();
            double distanciaClimb = 0.10; //Adicionar a distancia exata para climbar

            if(distanciaAtual > distanciaClimb){
                movimentos.tras(-0.3);
                movimentos.parar();
            }else if(distanciaClimb > distanciaAtual){
                movimentos.frente(0.3);
                movimentos.parar();
            } else {
                movimentos.parar();
                timer.reset();
                fase = 9;
            }
        } else if(fase == 9){
            movimentos.Climb(0.1);
        }
    }
}