package frc.robot;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class Vision {
    private final NetworkTable table;
    private final NetworkTableEntry idEntry;
    private final NetworkTableEntry xEntry;
    private final NetworkTableEntry yEntry;
    private final NetworkTableEntry zEntry;

    public Vision(String cameraName) {
        // Pega a tabela que o PhotonVision publica
        table = NetworkTableInstance.getDefault().getTable("photonvision/" + cameraName);
        
        // Usamos getEntry() que é o método padrão da NetworkTable para criar as entradas
        idEntry = table.getEntry("targetID"); 
        xEntry = table.getEntry("targetPose_TranslationX");
        yEntry = table.getEntry("targetPose_TranslationY");
        zEntry = table.getEntry("targetPose_TranslationZ");
    }

    /**
     * Retorna o ID da AprilTag detectada.
     * @return ID da tag ou -1 se nenhum alvo for visto.
     */
    public int getTargetID() {
        // Como GenericEntry pode ser qualquer coisa, especificamos .getInteger
        return (int) idEntry.getInteger(-1); 
    }

    /**
     * Calcula a distância linear direta até o alvo usando Pitágoras 3D.
     * @return Distância em metros (ou 0.0 se sem alvo).
     */
    public double getDistance() {
        if (getTargetID() == -1) return 0.0;

        // Buscamos os valores como Double
        double x = xEntry.getDouble(0.0);
        double y = yEntry.getDouble(0.0);
        double z = zEntry.getDouble(0.0);

        // Teorema de Pitágoras 3D: d = sqrt(x² + y² + z²)
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }
    
    /**
     * Verifica se a câmera está enxergando algum alvo.
     */
    public boolean hasTarget() {
        return table.getEntry("hasTarget").getBoolean(false);
    }
}