/*
* Retirement calculator - Allows one to run various retirement scenarios and see how much money you have to retire on.
*
* Copyright (C) 2016  Michael C. Wexler

* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*
* I can be reached at mike.wexler@gmail.com.
*
*/

package name.wexler.retirement.visualizer;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.JsonGenerationException;

import name.wexler.retirement.datastore.DataStore;
import name.wexler.retirement.visualizer.Asset.AssetAccount;
import name.wexler.retirement.visualizer.Tables.CashFlowCalendar;
import name.wexler.retirement.visualizer.CashFlowFrequency.CashFlowFrequency;
import name.wexler.retirement.visualizer.Asset.Asset;
import name.wexler.retirement.visualizer.CashFlowInstance.SecurityTransaction;
import name.wexler.retirement.visualizer.CashFlowEstimator.CashFlowEstimator;
import name.wexler.retirement.visualizer.Entity.Company;
import name.wexler.retirement.visualizer.Entity.Person;
import name.wexler.retirement.visualizer.Expense.Expense;
import org.sqlite.JDBC;


/**
 * Created by mwexler on 6/28/16.
 */
public class Retirement {
    private List<Scenario> scenarios;
    private List<Person> people;
    static private DataStore ds;


    Retirement() {
        Context context = new Context();

        try {
            this.people = Person.readPeople(context);
            Expense.readExpenses(context);
            Company.readCompanies(context);
            AssetAccount.readAssetAccounts(context);
            Job.readJobs(context);
            CashFlowFrequency.readCashFlowFrequencies(context);
            Security.readSecurities(context, ds);
            CashFlowEstimator.readCashFlowSources(context);
            Asset.readAssets(context);

            this.scenarios = Scenario.readScenarios(context);
        } catch (JsonGenerationException | JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Scenario getScenario() {
        return scenarios.get(0);
    }

    static public DataStore getDataStore() { return ds; }

    public List<Person> getPeople() {
        return people;
    }


    public CashFlowCalendar getCashFlowCalendar(String scenarioId) {
        return getScenario().getCashFlowCalendar();
    }


    static {
        try {
            DriverManager.registerDriver(new JDBC());
        } catch (SQLException var1) {
            var1.printStackTrace();
        }
        ds = new DataStore();
    }
}

