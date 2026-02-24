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
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
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
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.IntegerSubscriber;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;



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

  // ===== CHASSI e Movimentos =====
  DifferentialDrive chassi;

  // Puxando todos os métodos e classes do arquivo Movimentos, para ser utilizado no autonomos
  Movimentos movimentos;

  // Puxa todos os dados da Raspberry pi em python
  Vision vision;
   
  // Variáveis do NetworkTables
  private NetworkTable table;
  private IntegerSubscriber idSub;
  private DoubleSubscriber yawSub;

  // Declaração dos Motores
  public MotorController motorEsquerdaMestre = new WPI_VictorSPX(1);// 1 e 2 esquerda
  public MotorController motorEsquerda       = new WPI_VictorSPX(2);
  public MotorController motorDireitaMestre  = new WPI_VictorSPX(3);
  public MotorController motorDireita        = new WPI_VictorSPX(4);
  public MotorController motorClimberMestre = new WPI_VictorSPX(5); //CLIMBER
  public MotorController motorClimber = new WPI_VictorSPX(6); // CLIMBER



  // Ligar motor NEO / ID
  public SparkMax ShooterPreto = new SparkMax(10, MotorType.kBrushless); // LADO ESQUERDO
  public SparkMax ShooterLaranja = new SparkMax(7, MotorType.kBrushless); // LADO DIREITO
  public SparkMax Pegar_Shooter = new SparkMax(9, MotorType.kBrushless); //MOTOR DE BAIXO
  public SparkMax Esteira = new SparkMax(8, MotorType.kBrushless); //ESTEIRA
  //Declaração de Spark com numero de RPM
  private SparkMaxConfig shooterConfig;
  // Definição entradas de temperatura no Shuffleboard
  private GenericEntry shooterPretoTempEntry;
  private GenericEntry shooterLaranjaTempEntry;
  // Entradas para velocidade/potência no Shuffleboard
  private GenericEntry shooterPretoSpeedEntry;
  private GenericEntry shooterLaranjaSpeedEntry;
  private GenericEntry pegarShooterSpeedEntry;
  //Entradas Para o RPM do Shooter
  private GenericEntry rpmPretoEntry;
  private GenericEntry rpmLaranjaEntry;
  // Declaração do tempo
  private double startTime;
  // Entradas específicas para o Shuffleboard visão
  private GenericEntry visaoHasTargetEntry;
  private GenericEntry visaoIdEntry;
  private GenericEntry visaoDistanciaEntry;
 
  // Definição controle
  XboxController controle = new XboxController(1);
  //XboxController controle2 = new XboxController(0);

 // Declaração Autonomos
  Autonomous autonomo;

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
     // Criar as entradas para o RPM (Valor inicial 0.0)
    rpmPretoEntry = Shuffleboard.getTab("Shooter")
        .add("RPM Real Preto", 0.0)
        .withPosition(4, 0) // Coluna 4, Linha 0
        .getEntry();

    rpmLaranjaEntry = Shuffleboard.getTab("Shooter")
        .add("RPM Real Laranja", 0.0)
        .withPosition(6, 0) // Coluna 6, Linha 0
        .getEntry();
    

    // Definição para motores andarem juntos
    ((IFollower) motorEsquerda).follow((IMotorController) motorEsquerdaMestre);
    ((IFollower) motorDireita).follow((IMotorController) motorDireitaMestre);

    //Motores do Climber
    ((IFollower) motorClimber).follow((IMotorController) motorClimberMestre);

    // Invertendo um lado
    motorDireitaMestre.setInverted(true);
    motorDireita.setInverted(true);

    //Definição Chassi
    chassi = new DifferentialDrive(motorEsquerdaMestre, motorDireitaMestre);
    
    // Inicializa a classe Movimentos
    movimentos = new Movimentos(chassi);

   //CAMERA
    vision = new Vision("Camera_TecRobot");

    //Inicializa a classe Autonomous
    autonomo = new Autonomous(movimentos, vision);
 
    // Declaração dos botões para escolher o autônomo no Shuffleboard
    auto_Chooser.setDefaultOption("AutonomoEsquerda", "Esquerda");
    auto_Chooser.setDefaultOption("AutonomoDireita", "Direita");
    auto_Chooser.setDefaultOption("AutonomoMeio", "Meio");

    ShuffleboardTab tab = Shuffleboard.getTab("Autônomos");
    tab.add("Escolher Autônomo", auto_Chooser);

    //Shuffle para a visão
    // Criando a aba de Visão no Shuffleboard
    ShuffleboardTab visaoTab = Shuffleboard.getTab("Visão");

    visaoHasTargetEntry = visaoTab.add("Tem Alvo", false)
      .withWidget(BuiltInWidgets.kBooleanBox) // Cria um quadrado que brilha (verde/vermelho)
      .withPosition(0, 0)
      .getEntry();

    visaoIdEntry = visaoTab.add("ID da Tag", -1)
      .withPosition(1, 0)
      .getEntry();

    visaoDistanciaEntry = visaoTab.add("Distância (m)", 0.0)
      .withPosition(2, 0)
      .getEntry();
  }

  @SuppressWarnings("removal")
  @Override
  public void robotInit() {
    // 1. Inicializa a instância do NetworkTables
    NetworkTableInstance inst = NetworkTableInstance.getDefault();

    // 2. Acessa a tabela "VisionData" (deve ser o mesmo nome usado no Python da Raspberry)
    table = inst.getTable("VisionData");

    // 3. Configura os "assinantes" para ler o ID e o Yaw
    // O -1 e o 0.0 são os valores padrão caso a Raspberry esteja desligada
    idSub = table.getIntegerTopic("targetID").subscribe(-1);
    yawSub = table.getDoubleTopic("targetYaw").subscribe(0.0);
    
    // Opcional: Mostrar no SmartDashboard que a visão iniciou
    SmartDashboard.putString("Status Visao", "Conectado ao NT");

    //SparkMaxConfig para definir parâmetros do motor
    shooterConfig = new SparkMaxConfig();
    shooterConfig.closedLoop
        .p(0.0001)           // Ajuste fino: se oscilar muito, diminua
        .velocityFF(0.00018); // Ajuste principal: para o motor NEO chegar perto do RPM alvo

    // 2. Aplicar configuração ao motor do lado direito (Laranja)
    // O ResetMode.kResetSafeParameters garante que configurações antigas sejam limpas
    ShooterLaranja.configure(shooterConfig, SparkBase.ResetMode.kResetSafeParameters, SparkBase.PersistMode.kNoPersistParameters);

    // 3. Inicializar o controlador que você declarou lá em cima
    m_closedLoopController = ShooterLaranja.getClosedLoopController();

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
    
    try {
        // Pega a velocidade atual dos encoders e envia para a Shuffleboard
        rpmPretoEntry.setDouble(ShooterPreto.getEncoder().getVelocity());
        rpmLaranjaEntry.setDouble(ShooterLaranja.getEncoder().getVelocity());
    } catch (Exception e) {
        // Silencia erros caso os motores não estejam conectados
    }

    // --- CÓDIGO DE TESTE DA VISÃO ---
    if (vision != null) {
        visaoHasTargetEntry.setBoolean(vision.hasTarget());
        visaoIdEntry.setDouble(vision.getTargetID());
        visaoDistanciaEntry.setDouble(vision.getDistance());
      }
  }

  @Override
  public void autonomousInit() {
   selectedAuto = auto_Chooser.getSelected();
    System.out.println("Modo autônomo selecionado:" + selectedAuto);

    // Iniciando o timer do autonomus
      startTime = Timer.getFPGATimestamp();
      SmartDashboard.putNumber("Tempoinicial", startTime);

  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
   double time = Timer.getFPGATimestamp();
    SmartDashboard.putNumber("Tempoautonomos", time);
    /* essa lógica serve para selecionar de qual posição o robo partirá no modo Autonomus */
    switch (selectedAuto) {
      case "Meio":
        autonomo.Meio();
         break;
      case "Esquerda":
        autonomo.Esquerda();
        break;
      case "Direita":
        autonomo.Direita();
        break;
      default:
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

 // CONTROLE 1
    double speed = -controle.getLeftY();//Joystick lado esquerdo
    double turn = -controle.getRightX();//Joystick lado direito
    boolean b = controle.getBButton();//Expulsa bola do reservatório
    boolean y = controle.getYButton();//Shooter - LANÇAR
    boolean quickTurn = controle.getRawButton(6);//girar no proprio eixo
    double pegar = controle.getRightTriggerAxis();// gatilho lado Direito
    double acelerar = controle.getLeftTriggerAxis();// gatilho lado Esquerda

    // CONTROLE 2
    boolean a = controle.getAButton(); // ESTEIRA
    boolean x = controle.getXButton(); // ESTEIRA
    //double CLIMBSOBE = controle2.getRightTriggerAxis(); // Gatilho Lado Direito
    //double CLIMBEDESCE = controle2.getLeftTriggerAxis(); // Gatilho Lado Esquerda

    

   // 1. Lê os dados que vêm da Raspberry via NetworkTables
    //long idVisto = idSub.get();
    //double erroYaw = yawSub.get();

    // DEFINIÇÃO DO QUE FAZ CADA BOTÃO

     if (acelerar>=0.1) { /* 0.1 pq é necessario um valor double ou int*/    
      // Acelerador
      speed = speed * 0.8;
      turn = turn * 0.8;
    } else {
      speed = speed * 0.5;
      turn = turn * 0.5;
    }
 // ESSTEIRA / CONTROLE 2
    if (a) {
      Esteira.set (0.75); // ESTEIRA LARGAR
    } else if (x) {
      Esteira.set (-0.75); // ESTEIRA PEGAR
    } else {
      Esteira.set (0); // ESTEIRA PARAR
    }
// Logica do Climb / CONTROLE 2
    /*if(CLIMBSOBE >= 0.1){
      motorClimberMestre.set(-0.3);
    } else if(CLIMBEDESCE >= 0.1){
      motorClimberMestre.set(0.3);
    } else {
      motorClimberMestre.set(0);
    }*/
        
    if(pegar >= 0.1){  // Recolhe combustivel
      ShooterPreto.set(0.8);
      Pegar_Shooter.set(0.65);
      Esteira.set(0.5);
    }else if(b) { // Devolve combustìvel
      ShooterPreto.set(-0.5);
      Pegar_Shooter.set(-0.5);
    }else if (y) {
    // Inicia o timer apenas na borda de subida do botão Y
      if (!yStarted) {
        timerTele.reset();
        timerTele.start();
        yStarted = true;
      }
      // Enquanto Y pressionado, após 1s roda os dois em potência máxima/negativa como desejado
      if (timerTele.get() > 0.5) {
        ShooterPreto.set(-0.8);   // primeiro motor
        ShooterLaranja.set(-0.8); // segundo motor
        Pegar_Shooter.set(0.8);
      } else {
        // comportamento durante o delay (ex.: roda só um motor ou potência reduzida)
        ShooterPreto.set(0.1);
        ShooterLaranja.set(-0.8);  // segundo motor
        Pegar_Shooter.set(-0.75);
    }
  }  else {
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