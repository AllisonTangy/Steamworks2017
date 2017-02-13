package org.usfirst.frc.team2412.robot;

import org.usfirst.frc.team2412.robot.sd.SmartDashboardUtils;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class DriveBaseController implements RobotController {

	private RobotDrive rd;
	private Joystick js;
	private Encoder encoder;

	public static double ROTATION_SPEED = 1.0;
	public static double DRIVE_SPEED = 1.0;

	/**
	 * 
	 * @param j
	 *            - the joystick
	 * @param l1
	 *            - front left motor
	 * @param l2
	 *            - back left motor
	 * @param r1
	 *            - front right motor
	 * @param r2
	 *            - back right motor
	 */
	public DriveBaseController(Joystick j, int l1, int l2, int r1, int r2) {
		rd = new RobotDrive(new CANTalon(l1), new CANTalon(l2), new CANTalon(r1), new CANTalon(r2));
		js = j;
	}

	/**
	 * Drives robot in coordination with controller.
	 */

	public void processTeleop() {
		double jsY = -js.getY();
		double jsX = -js.getX();

		double jsTwist = -js.getTwist(); // getRawAxis(3) is for the new
											// joystick, getTwist is for the
											// logitech joystick.
		if (js.getRawButton(5)) {
			// Drive like airplane
			rd.arcadeDrive(jsY, jsX, true);			
		} else {
			// Drive with twist
			rd.arcadeDrive(jsY, jsTwist, true);
		}
	}

	private int stage = 0;
	private boolean done = false;
	private double initDist = Double.NaN;
	private NetworkTable table = NetworkTable.getTable(VisionController.TABLENAME);
	private double lastD, lastA = Double.NaN;

	public void processAutonomous() {
		if (done)
			return;
		if (encoder == null)
			encoder = new Encoder(6, 7); // channel a and channel b
		try {
			switch (stage) {
			case 0:
				rd.arcadeDrive(.8d, 0d, false);
				if (encoder.getDistance() >= 15d /** or other constant we determine **/) {
					stage = 1;
				}
				break;
			case 1:
				if (SmartDashboardUtils.getRobotPosition(false) == 0) {
					rd.arcadeDrive(0d, .5d, false);
					Timer.delay(.5d);
					rd.arcadeDrive(0d, 0d, false);
					stage = 2;
				} else if (SmartDashboardUtils.getRobotPosition(false) == 2) {
					rd.arcadeDrive(0d, .5d, false);
					Timer.delay(.5d);
					rd.arcadeDrive(0d, 0d, false);
					stage = 2;
				}
				break;
			case 2:
				try {
					if (table.getNumber("distance", Double.NaN) < Constants.AUTO_SECOND_STEP_DIST) {
						stage = 3;
						lastD = table.getNumber("distance", 2d);
						lastA = table.getNumber("angle", -1d);
						initDist = lastD;
						return;
					} else {
						rd.arcadeDrive((initDist = (Double.isNaN(initDist) ? table.getNumber("distance", Double.NaN) : initDist)) / table.getNumber("distance", Double.NaN) + 0.1d, .8d * table.getNumber("angle", Double.NaN), true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 3:
				if (table.getNumber("distance", lastD) == lastD || table.getNumber("angle", lastA) == lastA) {
					Timer.delay(0.15d);
					return;
				}
				if (table.getNumber("distance", 2) < Constants.AUTO_FINAL_DIST) {
					done = true;
					return;
				} else {
					rd.arcadeDrive((initDist = (Double.isNaN(initDist) ? table.getNumber("distance", Double.NaN) : initDist)) / (table.getNumber("distance", Double.NaN) * 2d) + 0.1d, .8d * table.getNumber("angle", Double.NaN), true);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Timer.delay(.15d);

	}

	public void teleopInit() {

	}

	public void autonomousInit() {

	}

}
