package frc.robot;
// Importação das bibliotecas
import com.ctre.phoenix.motorcontrol.IFollower;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Movimentos {

    private DifferentialDrive chassi;
    
    //===== Construtot recebe o chassi ========

      public Movimentos(DifferentialDrive chassi) {
        this.chassi = chassi;
        
    }

    // ================ Movimentos Basicos ==============

    public void frente(double velocidade) {
        chassi.curvatureDrive(velocidade, 0.0, false);
    }

    public void tras(double velocidade) {
        chassi.curvatureDrive(-velocidade, 0.0, false);
    }

    public void direita(double velocidade) {
        chassi.curvatureDrive(0.0, 0.1, true);
    }

    public void esquerda(double velocidade) {
        chassi.curvatureDrive(0.0, -0.1, true);
    }
   
    // Curva enquanto anda para frente (diagonal)
    // intensidadeCurva = -1.0 (esquerda) até 1.0 (direita)
    // Diagonal: velocidade > 0 para frente, velocidade < 0 para trás
    public void diagonal(double velocidade, double intensidadeCurva) {
        chassi.curvatureDrive(velocidade, intensidadeCurva, false);
    }

    public void parar() {
        chassi.curvatureDrive(0.0, 0.0, false);
    }
}