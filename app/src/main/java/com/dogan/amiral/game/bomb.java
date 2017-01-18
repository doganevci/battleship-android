package com.dogan.amiral.game;

import com.dogan.amiral.game.enums.bombType;

public class bomb {

    public bombType type;

    int sizeOfDamage;
    int numberOfWeapons;
    String name;


    public bomb(bombType TYPE)
    {
        type=TYPE;
        name=TYPE.toString();

        switch (TYPE) {
            case NORMALFIRE:
                sizeOfDamage=0;       //means that, fire affect at that point only
                numberOfWeapons=-1;   // Unlimited :D
                break;
            case THEBOMB:
                sizeOfDamage=1;
                numberOfWeapons=2;   // Unlimited :D
                break;

            case HYDROGENBOMB:
                sizeOfDamage=2;
                numberOfWeapons=1;
                break;

            case PROBE:
                sizeOfDamage=-3;   // minus means not damage only show the area
                numberOfWeapons=1;
                break;
        }


    }
}
