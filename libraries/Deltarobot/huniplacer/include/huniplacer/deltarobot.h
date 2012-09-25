//******************************************************************************
//
//                 Low Cost Vision
//
//******************************************************************************
// Project:        huniplacer
// File:           deltarobot.h
// Description:    symbolizes an entire deltarobot
// Author:         Lukas Vermond & Kasper van Nieuwland
// Notes:          -
//
// License:        GNU GPL v3
//
// This file is part of huniplacer.
//
// huniplacer is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// huniplacer is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with huniplacer.  If not, see <http://www.gnu.org/licenses/>.
//******************************************************************************


#pragma once

#include <modbus/modbus.h>
#include <huniplacer/Point3D.h>
#include <huniplacer/imotor3.h>
#include <huniplacer/effector_boundaries.h>
#include <huniplacer/huniplacer.h>

//TODO: implement forward kinematics and use that to calculate the current effector position

/**
 * @brief holds huniplacer related classes
 **/
namespace huniplacer
{
	class InverseKinematicsModel;

	/**
	 * @brief this class symbolises an entire deltarobot
	 **/
    class deltarobot 
    {
        private:
            InverseKinematicsModel& kinematics;
            steppermotor3& motors;
            effector_boundaries* boundaries;

            Point3D effector_location;
            bool boundaries_generated;

            bool is_valid_angle(double angle);
        
        public:
            /**
             * @brief constructor
             * @param kinematics kinematics model that will be used to convert points to motions
             * @param motors implementation of motor interface that will be used to communicate with the motors
             **/
            deltarobot(InverseKinematicsModel& kinematics, steppermotor3& motors);

            ~deltarobot(void);
            
            inline effector_boundaries* get_boundaries();
            inline bool has_boundaries();
            void generate_boundaries(double voxel_size);

            /**
			 * @brief checks the path between two points
			 * @param begin start point
			 * @param end finish point
			 **/
            bool check_path(const Point3D& begin,const Point3D& end);

            /**
             * @brief makes the deltarobot move to a point
             * @param p 3-dimensional point to move to
             * @param speed movement speed speed in degrees per second
             * @param async motions will be stored in a queue for later execution if true
             **/
            void moveto(const Point3D& p, double speed, bool async = true);

            void calibrateMotor(modbus_t* modbus, int motorIndex);
            bool checkSensor(modbus_t* modbus, int sensorIndex);
            void calibrateMotors(modbus_t* modbus);

            /**
             * @brief stops the motors
             **/
            void stop(void);

            /**
             * @brief wait for the deltarobot to become idle
             *
             * the deltarobot is idle when all it's motions are completed
             * and it has stopped moving
             *
             * @param timeout time in milliseconds for the wait to timeout. 0 means infinite
             **/
            bool wait_for_idle(long timeout = 0);

            /**
             * @brief true if the deltarobot is idle, false otherwise
             **/
            bool is_idle(void);

            /**
             * @brief shuts down the deltarobot's hardware
             */
            void power_off(void);

            /**
             * @brief turns on the deltarobot's hardware
             */
            void power_on(void);

            Point3D& getEffectorLocation();
    };

    effector_boundaries* deltarobot::get_boundaries(){return boundaries;}
    bool deltarobot::has_boundaries(){return boundaries_generated;}
}
