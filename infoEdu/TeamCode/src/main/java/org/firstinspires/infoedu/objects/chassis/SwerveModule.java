package org.firstinspires.infoedu.objects.chassis;

import com.arcrobotics.ftclib.controller.PIDFController;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
public class SwerveModule {

    DcMotor motor;
    CRServo servo;
    AbsoluteAnalogEncoder encoder;

    public static double target = 0;
    boolean motorDirection = true;
    public static double P = 0.013, I = 0.0002, D = 0, F = 0;
    // P=0.007 I=0 D=0.00015

    public static double tolerance = 1;
    PIDFController angleController = new PIDFController(P, I, D, F);
    public SwerveModule(DcMotor motor, CRServo servo, AbsoluteAnalogEncoder encoder) {
        this.motor = motor; this.servo = servo; this.encoder = encoder;

        angleController.setTolerance(tolerance);
    }

    public void drive(double motorPower, double servoAngle) {

        //se inverseaza si se normalizeaza unghiul
        target = servoAngle + 180;
        target = 360 - target;

        double current = encoder.getCurrentPosition();
        double error;

        //se calculeaza arcul minim si PID-ul actioneaza in functie de el
        error = CalculateTarget(target, current);
        double power = angleController.calculate(0, error);
        if ( Math.abs(error) < tolerance )
            power = 0;

        //daca nu mai primesc input din gamepad, rotile raman la ultima pozitie
        if (motorPower < 0.01){
            servo.setPower(0.0d);}
        else{
            servo.setPower(power);}
        //se da putere la motoare si servouri
        if(motorDirection == true)
            motor.setPower(motorPower);
        else
            motor.setPower(-motorPower);
    }

    private double CalculateTarget(double target, double current){
        double target1 = target, target2 = (target+180)%360;
        double errorpoz = 0.0d, errorneg = 0.0d;
        double errorpoz1 = 0.0d, errorneg1 = 0.0d;

        double output = 0.0d;


        //gaseste cel mai mic arc de cerc
        if ( current >= target1){
            errorpoz = (360 - current) + target1;
            errorneg = current - target1;
        }
        else{
            errorpoz = target1 - current;
            errorneg = current + ( 360 - target1 );
        }

        if ( current >= target2){
            errorpoz1 = (360 - current) + target2;
            errorneg1 = current - target2;
        }
        else{
            errorpoz1 = target2 - current;
            errorneg1 = current + ( 360 - target2 );
        }

        errorpoz*=-1;
        errorpoz1*=-1;

        double min = 1000;
        min = Math.min(Math.abs(errorpoz), min);
        min = Math.min(Math.abs(errorpoz1), min);
        min = Math.min(Math.abs(errorneg), min);
        min = Math.min(Math.abs(errorneg1), min);


        //returneaza puterea necesara
        if ( Math.abs(errorpoz) == min ) {
            motorDirection = true;
            return errorpoz;

        }

        if ( Math.abs(errorneg) == min ) {
            motorDirection = true;
            return errorneg;
        }

        if ( Math.abs(errorpoz1) == min ) {
            motorDirection = false;
            return errorpoz1;
        }

        if ( Math.abs(errorneg1) == min ) {
            motorDirection = false;
            return errorneg1;
        }
        return output;
    }
}
