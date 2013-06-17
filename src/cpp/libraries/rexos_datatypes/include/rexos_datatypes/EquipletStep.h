/* 
 * File:   EquipletStep.h
 * Author: alexander-ubuntu
 *
 * Created on June 16, 2013, 3:33 PM
 */
#ifndef EQUIPLETSTEP_H
#define	EQUIPLETSTEP_H

#include <string.h>
#include <iostream>
#include <map>
#include <utility>
#include "rexos_datatypes/InstructionData.h"
#include "rexos_datatypes/TimeData.h"
#include <libjson/libjson.h>

namespace rexos_datatypes{
    
    class EquipletStep {
    public:
        EquipletStep(JSONNode n);
        virtual ~EquipletStep();

        std::string getId();
        void setId(std::string id);

        std::string getServiceStepID();
        void setServiceStepID(std::string serviceStepID);

        std::string getNextStep();
        void setNextStep(std::string nextStep);

        int getModuleId();
        void setModuleId(int id);

        InstructionData getInstructionData();
        void setInstructionData(InstructionData instructionData);

        int getStatus();
        void setStatus(int status);

        std::map<std::string, std::string> getStatusData();
        void setStatusData(map<std::string, std::string> statusData);

        TimeData getTimeData();
        void setTimeData(TimeData timeData);
    private:
            std::string _id;
            std::string serviceStepID;
            std::string nextStep;
            int moduleId;
            InstructionData instructionData;
            int status;
            std::map<std::string, std::string> statusData;
            TimeData timeData;
            void setValues(const JSONNode & n);
            InstructionData setInstructionDataFromNode(const JSONNode & n);
            TimeData setTimeDataFromNode(const JSONNode & n);
            std::map<std::string, std::string> setMapFromNode(const JSONNode & n);
    };
}
#endif	/* EQUIPLETSTEP_H */
