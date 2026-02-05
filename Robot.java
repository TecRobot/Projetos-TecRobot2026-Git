package frc.robot;
// Importação das bibliotecas
import com.ctre.phoenix.motorcontrol.IFollower;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkBase;




public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private final SendableChooser<String> auto_Chooser = new SendableChooser<>();
  private String selectedAuto;
  private SparkClosedLoopController m_closedLoopController; // Controlador de PID/SmartMotion
  
  // Puxando todos os métodos e classes do arquivo Timer, para ser utilizado no autonomos
  private Timer timerAuto = new Timer();//timer autonomous
  private Timer timerTele = new Timer();//timer teleoperado
  private boolean yStarted = false;

  
  //Declaração do limitador
  private SlewRateLimiter aceleracaoSuave;

    /*
   * SlewRateLimiter controla quanto a velocidade pode variar por segundo.
   * Valor 2.5 → leva ~0.4s para ir de 0 a 100%.
   * Isso evita tranco mecânico e picos de corrente.
   */
  private SlewRateLimiter shooterRateLimiter = new SlewRateLimiter(2.5);

   // Velocidade desejada do shooter (setpoint)
   private double shooterTargetSpeed = 0.0;
                      
 

  // ===== CHASSI e Movimentos =====
  DifferentialDrive chassi;

  // Puxando todos os métodos e classes do arquivo Movimentos, para ser utilizado no autonomos
  Movimentos movimentos;
   

  // Declaração dos Motores
  public MotorController motorEsquerdaMestre = new WPI_VictorSPX(4);
  public MotorController motorEsquerda       = new WPI_VictorSPX(6);
  public MotorController motorDireitaMestre  = new WPI_VictorSPX(13);
  public MotorController motorDireita        = new WPI_VictorSPX(2);

  // Ligar motor NEO / ID
  public SparkMax ShooterPreto = new SparkMax(3, MotorType.kBrushless); // LADO ESQUERDO
  public SparkMax ShooterLaranja = new SparkMax(2, MotorType.kBrushless); // LADO Direito
  public SparkMax Pegar_Shooter = new SparkMax(11, MotorType.kBrushless); //motor de baixo
  // Definição entradas de temperatura no Shuffleboard
  private GenericEntry shooterPretoTempEntry;
  private GenericEntry shooterLaranjaTempEntry;
  private GenericEntry Pegar_ShooterTempEntry;
  // Entradas para velocidade/potência no Shuffleboard
  private GenericEntry shooterPretoSpeedEntry;
  private GenericEntry shooterLaranjaSpeedEntry;
  private GenericEntry pegarShooterSpeedEntry;
 
  // Definição controle
  XboxController controle = new XboxController(0);
  //XboxController controle2 = new XboxController(0);


  public Robot() {
    // Cria abas/entradas no Shuffleboard para mostrar temperatura
    shooterPretoTempEntry = Shuffleboard.getTab("Shooter").add("Shooter1 Temp (C)", 0.0).withPosition(0, 0).getEntry();
    shooterLaranjaTempEntry = Shuffleboard.getTab("Shooter").add("Shooter2 Temp (C)", 0.0).withPosition(2, 0).getEntry();
    // Criar entradas de velocidade (valor inicial 0)
    shooterPretoSpeedEntry = Shuffleboard.getTab("Shooter").add("ShooterPreto Speed", 0.0).withPosition(0, 1).getEntry();
    shooterLaranjaSpeedEntry = Shuffleboard.getTab("Shooter").add("ShooterLaranja Speed", 0.0).withPosition(2, 1).getEntry();
    pegarShooterSpeedEntry = Shuffleboard.getTab("Shooter").add("pegar Speed", 0.0).withPosition(4, 1).getEntry();
     m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
     m_chooser.addOption("My Auto", kCustomAuto);
     SmartDashboard.putData("Auto choices", m_chooser);
    

    // Definição para motores andarem juntos

    ((IFollower) motorEsquerda).follow((IMotorController) motorEsquerdaMestre);
    ((IFollower) motorDireita).follow((IMotorController) motorDireitaMestre);

    // Invertendo um lado

    motorDireitaMestre.setInverted(true);
    motorDireita.setInverted(true);

    //Definição Chassi

    chassi = new DifferentialDrive(motorEsquerdaMestre, motorDireitaMestre);
    
    // Inicializa a classe Movimentos
    movimentos = new Movimentos(chassi);

    //CAMERA
    //CameraServer.startAutomaticCapture();
  }
  
  @Override
  public void robotPeriodic() {
   try {
      shooterPretoTempEntry.setDouble(ShooterPreto.getMotorTemperature());
      shooterLaranjaTempEntry.setDouble(ShooterLaranja.getMotorTemperature());
     // Atualiza velocidade/potência no Shuffleboard.
     // Usamos try/catch porque alguns métodos podem variar entre versões da lib.
     try {
       shooterPretoSpeedEntry.setDouble(ShooterPreto.get());
       shooterLaranjaSpeedEntry.setDouble(ShooterLaranja.get());
       pegarShooterSpeedEntry.setDouble(Pegar_Shooter.get());
     } catch (Exception ex) {
       // se get() não existir, opcional: mostre uma variável local/target em vez disso
       // ex.: shooterPretoSpeedEntry.setDouble(shooterTargetSpeed);
     }
    } catch (Exception e) {
      // evita exceção se o método não existir na sua versão da lib
    }
  }

  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        /*Exemplo:
         * movimentos.frente(0.5, 0.0, false);
         */
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
 // ===== MOVIMENTAÇÃO CONTROLE =====
    boolean b = controle.getBButton();//expulsa bola do reservatório
    boolean y = controle.getYButton();//shooter
    boolean quickTurn = controle.getRawButton(6);//girar no proprio eixo
    double speed = -controle.getLeftY(); //joystick lado esquerdo
    double turn = -controle.getRightX();//joystick lado direito
    double pegar /*Pegar_Shooter*/ = controle.getRawAxis(3);//gatilho lado esquerdo
    double acelerar = controle.getRawAxis(2);//gatilho lado direito

  //Funções Controle2
  /*boolean b2 = controle2.getBButton();
    boolean y2 = controle2.getYButton();
    double pegar2 = controle2.getRawAxis(3);
 
// 1. Mapeia o que qualquer um dos dois pilotos quer fazer
    boolean querPegar    = (Pegar_Shooter >= 0.1 || pegar2 >= 0.1); 
    boolean querAtirar   = (y || y2);
    boolean querDevolver = (b || b2);

    // 2. Verifica se existem comandos conflitantes ao mesmo tempo
    boolean conflito = (querPegar && querAtirar) || 
                       (querPegar && querDevolver) || 
                       (querAtirar && querDevolver);

    // 3. Execução com Prioridade de Segurança

    if(conflito){
      ShooterPreto.set(0);
      ShooterLaranja.set(0);
      Pegar_Shooter.set(0);
    }else if(querPegar) {
    // Coloca combustivel no reservatório
      ShooterPreto.set(0.8);
      //ShooterLaranja.set(0.3); 
      Pegar_Shooter.set(0.45);
    }else if(querAtirar){
// Inicia o timer apenas na borda de subida do botão Y
      if (!yStarted) {
        timerTele.reset();
        timerTele.start();
        yStarted = true;
      }
// Enquanto Y pressionado, após 1s roda os dois em potência máxima/negativa como desejado
      if (timerTele.get() > 1.0) {
        ShooterPreto.set(-0.8);   // primeiro motor
        ShooterLaranja.set(0.7);  // segundo motor
        Pegar_Shooter.set(0.8);
      } else {
// comportamento durante o delay (ex.: roda só um motor ou potência reduzida)
        ShooterPreto.set(0.1);
        ShooterLaranja.set(0.7);
        Pegar_Shooter.set(0);
      }
    } else if (querDevolver){
// Devolve combustìvel
      ShooterPreto.set(-0.5);
//ShooterLaranja.set(-0.5);
      Pegar_Shooter.set(-0.5);
    } else {
 // Parar o timer e resetá-lo
      timerTele.stop();
      timerTele.reset();
// Definir como falso a codição que começa o timer de volta
      yStarted = false;
// Parar Robô
      ShooterPreto.set(0);
      ShooterLaranja.set(0);
      Pegar_Shooter.set(0);
    }
    
    chassi.curvatureDrive(speed, turn, quickTurn); 
   */

    // DEFINIÇÃO DO QUE FAZ CADA BOTÃO

     if (acelerar>=0.1) { /* 0.1 pq é necessario um valor double ou int*/    
      // Acelerador
      speed = speed * 0.8;
      turn = turn * 0.8;
    } else {
      speed = speed * 0.5;
      turn = turn * 0.5;
    }
    if(pegar >= 0.1){
      // Coloca combustivel no reservatório
      ShooterPreto.set(0.8);
      //ShooterLaranja.set(0.3); 
      Pegar_Shooter.set(0.65);
    }else if(b) {
      // Devolve combustìvel
      ShooterPreto.set(-0.5);
      //ShooterLaranja.set(-0.5);
      Pegar_Shooter.set(-0.5);
    }else if (y) {
      // Inicia o timer apenas na borda de subida do botão Y
      if (!yStarted) {
        timerTele.reset();
        timerTele.start();
        yStarted = true;
      }
      // Enquanto Y pressionado, após 1s roda os dois em potência máxima/negativa como desejado
      if (timerTele.get() > 1.0) {
        ShooterPreto.set(-0.8);   // primeiro motor
        ShooterLaranja.set(0.7);  // segundo motor
        Pegar_Shooter.set(0.8);
      } else {
        // comportamento durante o delay (ex.: roda só um motor ou potência reduzida)
        ShooterPreto.set(0.1);
        ShooterLaranja.set(0.7);
        Pegar_Shooter.set(0);
    }
  }
    else{
      // Parar o timer e resetá-lo
      timerTele.stop();
      timerTele.reset();
      // Definir como falso a codição que começa o timer de volta
      yStarted = false;
     // Parar Robô
      ShooterPreto.set(0);
      ShooterLaranja.set(0);
      Pegar_Shooter.set(0);
    }
    chassi.curvatureDrive(speed, turn, quickTurn);
    
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
