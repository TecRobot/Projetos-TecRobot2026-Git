package frc.robot;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;


public class Movimentos {

    private DifferentialDrive chassi;

    private SparkMax ShooterPreto;
    private SparkMax ShooterLaranja;
    private SparkMax Pegar_Shooter;
    private SparkMax Climb;
    
    //===== Construtot recebe o chassi ========

      public Movimentos(DifferentialDrive chassi) {
        this.chassi = chassi;
        
    }

    public Movimentos(SparkMax ShooterPreto, SparkMax ShooterLaranja, SparkMax Pegar_Shooter, SparkMax Climb) {
        this.ShooterPreto = ShooterPreto;
        this.ShooterLaranja = ShooterLaranja;
        this.Pegar_Shooter = Pegar_Shooter;
        this.Climb = Climb;
    }

    // ================ Movimentos Basicos ==============

    public void frente(double velocidade) {
        chassi.curvatureDrive(velocidade, 0.0, false);
    }

    public void tras(double velocidade) {
        chassi.curvatureDrive(-velocidade, 0.0, false);
    }

    public void direita(double velocidade, double Rotation) {
        chassi.curvatureDrive(velocidade, Rotation, true);
    }

    public void esquerda(double velocidade, double Rotation) {
        chassi.curvatureDrive(velocidade, -Rotation, true);
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
        // ================ Movimentos do Shooter ==============

    public void atirar(double velocidade, double velocidade2, double velocidade3) {
        ShooterPreto.set(velocidade);
        ShooterLaranja.set(velocidade);
        Pegar_Shooter.set(velocidade);
    }

    public void pegar(double velocidade, double velocidade2) {
        Pegar_Shooter.set(velocidade);
        ShooterLaranja.set(velocidade);
    }
    


    public void Devolver(double velocidade, double velocidade2) {
        Pegar_Shooter.set(-velocidade);
        ShooterLaranja.set(-velocidade);
    }
    
    public void pararShooter() {
        ShooterPreto.set(0.0);
        ShooterLaranja.set(0.0);
        Pegar_Shooter.set(0.0);
    }

    public void Climb(double velocidade){

    }

}