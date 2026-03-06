package frc.robot;

import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.Timer;

public class Movimentos {

    private DifferentialDrive chassi;

    private SparkMax shooterPreto;
    private SparkMax shooterLaranja;
    private SparkMax pegarShooter;
    private MotorController ClimberD;
    private MotorController ClimberE;

    // Para a função do atirar
    private Timer timerShooter = new Timer();
    private boolean shooterIniciado = false;

    // ===== Construtor único =====
    public Movimentos(DifferentialDrive chassi,
                      SparkMax shooterPreto,
                      SparkMax shooterLaranja,
                      SparkMax pegarShooter,
                      MotorController ClimberD,
                       MotorController ClimberE) {

        this.chassi = chassi;
        this.shooterPreto = shooterPreto;
        this.shooterLaranja = shooterLaranja;
        this.pegarShooter = pegarShooter;
        this.ClimberD = ClimberD;
        this.ClimberE = ClimberE;
    }

    // ================ MOVIMENTOS DO CHASSI ================

    public void frente(double velocidade) {
        chassi.curvatureDrive(velocidade, 0.0, false);
    }

    public void tras(double velocidade) {
        chassi.curvatureDrive(-velocidade, 0.0, false);
    }

    public void direita(double velocidade, double rotacao) {
        chassi.curvatureDrive(velocidade, rotacao, false);
    }

    public void esquerda(double velocidade, double rotacao) {
        chassi.curvatureDrive(velocidade, -rotacao, false);
    }

    public void diagonal(double velocidade, double intensidadeCurva) {
        chassi.curvatureDrive(velocidade, intensidadeCurva, false);
    }

    public void parar() {
        chassi.curvatureDrive(0.0, 0.0, false);
    }

    // ================ SHOOTER ================

    // Todos na mesma velocidade
    public void atirar() {
     
        shooterPreto.set(-0.8);
        shooterLaranja.set(-0.8);
        pegarShooter.set(0.8);

    }

    public void pegar(double velocidade) {
       shooterPreto.set(0.8);
       pegarShooter.set(0.65);
    }


    public void pararShooter() {
        shooterPreto.set(0.0);
        shooterLaranja.set(0.0);
        pegarShooter.set(0.0);

        timerShooter.stop();
        timerShooter.reset();
        shooterIniciado = false;

    }

    public void Climb (){

    }
}