package frc.robot;

import edu.wpi.first.wpilibj.Timer;

public class Autonomous {
    private Timer timerGeral = new Timer();
    private Timer timer = new Timer();
    private Movimentos movimentos;
    private int fase;

    public Autonomous(Movimentos movimentos) {
        this.movimentos = movimentos;
    }

    public void setup() {
        timerGeral.reset();
        timer.reset();

        timerGeral.start();
        timer.start();
    }

    //Logica do meio
    public void Meio() {

    if (timerGeral.get() > 20.0) {
        movimentos.parar();
        return;
    }

    if (fase == 0) {
        movimentos.frente(0.4);

        if (timer.get() > 3) {
            movimentos.parar();
            timer.reset();
            fase = 1;
        }
    }

    else if (fase == 1) {
        movimentos.atirar(0.7, 0.8, 0.9);

        if (timer.get() > 5) {
            timer.reset();
            fase = 2;
        }
    }

    else if (fase == 2) {
        movimentos.diagonal(0.4, 0.1);

        if (timer.get() > 4) {
            movimentos.parar();
            fase = 3;
        }
    }

} 
//Meio dois, onde ele vai para espalhar as 24 bolas
public void meioDois() {
    if (timerGeral.get() > 20.0) {
        movimentos.parar();
        return;
    }

    if (fase == 0) {
        movimentos.frente(0.4);

        if (timer.get() > 3) {
            movimentos.parar();
            timer.reset();
            fase = 1;
        }
    }

    else if (fase == 1) {
        movimentos.atirar(0.7, 0.8, 0.9);

        if (timer.get() > 5) {
            timer.reset();
            fase = 2;
        }
    }

    else if (fase == 2) {
        movimentos.direita(0, 0.3);

        if (timer.get() > 1) {
            movimentos.parar();
            fase = 3;
            timer.reset();
        }
    } else if ( fase == 3){
        movimentos.frente(0.5);

        if (timer.get() > 3){
            movimentos.parar();
            fase = 4;
            timer.reset();
        }
    } else if ( fase == 4){
        movimentos.esquerda(0, 0.3);

        if (timer.get() > 1){
            movimentos.parar();
            timer.reset();
            fase = 5;
        }
    } else if ( fase == 5){
        movimentos.frente(0.5);

        if(timer.get() > 3){
            movimentos.parar();
            timer.reset();
            fase = 6;
        }
    } else if (fase == 6){

    double tempo = timer.get();

    // 0 a 1 segundo → gira no próprio eixo
        if (tempo < 1){
            movimentos.direita(0, -0.3); // exemplo de giro no eixo
        }   

    // 1 a 3 segundos → se mexe para frente e tras
        else if (tempo >= 1 && tempo < 3){
        
        // alterna frente e trás rapidamente
            if ((int)(tempo * 4) % 2 == 0){
                movimentos.frente(0.2);
            } else {
                movimentos.tras(-0.2);
            }
        }

    // depois de 3 segundos
        else {
            movimentos.parar();
            fase = 7; // próxima fase
            timer.reset();
            }
        }
    }


    public void Direita(){
        if (timerGeral.get() > 20.0) {
            movimentos.parar();
        return;
    }

    if( fase == 0){
        movimentos.direita(0, 0.3);

        if(timer.get() > 1){
            movimentos.parar();
            timer.reset();
            fase = 1;
        }
    } else if (fase == 1){
        movimentos.tras(-0.3);

        if(timer.get() > 3){
            movimentos.parar();
            timer.reset();
            fase = 2;
        }
    } else if(fase == 2){
        movimentos.atirar(0.7,0.8,0.8);

        if(timer.get() > 5){
            movimentos.parar();
            timer.reset();
            fase = 3;
        }
    } else if(fase == 3){
        movimentos.diagonal(0.4, 0.1);

        if(timer.get() > 4) {
            movimentos.parar();
            timer.reset();
            fase = 4;
        }
    } else if(fase == 4){
            double tempo = timer.get();

    // 0 a 1 segundo → gira no próprio eixo
        if (tempo < 1){
            movimentos.direita(0, -0.3); // exemplo de giro no eixo
        }   

    // 1 a 3 segundos → se mexe para frente e tras
        else if (tempo >= 1 && tempo < 3){
        
        // alterna frente e trás rapidamente
            if ((int)(tempo * 4) % 2 == 0){
                movimentos.frente(0.2);
            } else {
                movimentos.tras(-0.2);
            }
        }

    // depois de 3 segundos
        else {
            movimentos.parar();
            fase = 7; // próxima fase
            timer.reset();
            }

        }
    
    }

    public void Esquerda(){
        if (timerGeral.get() > 20.0) {
            movimentos.parar();
        return;
    }

    if( fase == 0){
        movimentos.esquerda(0, 0.3);

        if(timer.get() > 1){
            movimentos.parar();
            timer.reset();
            fase = 1;
        }
    } else if (fase == 1){
        movimentos.tras(-0.3);

        if(timer.get() > 3){
            movimentos.parar();
            timer.reset();
            fase = 2;
        }
    } else if(fase == 2){
        movimentos.atirar(0.7,0.8,0.8);

        if(timer.get() > 5){
            movimentos.parar();
            timer.reset();
            fase = 3;
        }
    } else if(fase == 3){
        movimentos.diagonal(0.5, -0.1);

        if(timer.get() > 6) {
            movimentos.parar();
            timer.reset();
            fase = 4;
        }
    
    }

    }
    
}
