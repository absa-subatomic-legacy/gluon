#Atomist Configuration
1. Copy the atomist registration output from the quantum mechanic/atomist client start up log into atomist-registration.json. 
   The default atomist-registration.json shows an example of what the registration json from atomist looks like.
2. Run the GenerateAtomistConfig script 

        python GenerateAtomistConfig.py
3. The script will generate `application-local.yml`. 
   This config file can be placed in the `src/main/resources` folder and can be used by running gluon with the command
   
        ./mvnw spring-boot:run --spring.profiles.active=local