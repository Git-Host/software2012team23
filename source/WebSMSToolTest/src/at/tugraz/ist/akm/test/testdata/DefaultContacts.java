package at.tugraz.ist.akm.test.testdata;

public class DefaultContacts
{

    private int mEnlargementFactor = 5;
    private int mEnlargementFactorNegativeContacts = 2;


    public String[][] getDefaultRecords()
    {
        String[][] baseRecords = { { "Foo", "Bar", "01906666" },
                { "Fraunz", "Huaba", "43680123456" },
                { "Sepp", "Schnoacha", "0680123457" },
                { "Randy", "Andy", "43680123458" }, { "Pope", "Joke", "6666" },
                { "Franziska van Drüben", "Venus", "43680664658" } };

        return enlarge(baseRecords, mEnlargementFactor);
    }


    public String[][] getNegativeRecords()
    {
        String[][] baseRecords = { { "RandyRandy", "AndyAndy", "43680123458999" } };
        return enlarge(baseRecords, mEnlargementFactorNegativeContacts);
    }


    private String[][] enlarge(String[][] baseRecords, int enlargementFactor)
    {
        int numContacts = baseRecords.length * enlargementFactor;

        String[][] records = new String[numContacts][baseRecords[0].length];

        for (int idx = 0; idx < numContacts; idx++)
        {
            records[idx][0] = baseRecords[idx % baseRecords.length][0];
            records[idx][1] = baseRecords[idx % baseRecords.length][1] + "-"
                    + Integer.toString(idx);
            records[idx][2] = baseRecords[idx % baseRecords.length][2]
                    + Integer.toString(idx);
        }
        return records;
    }
}
