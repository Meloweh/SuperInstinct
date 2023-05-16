/**
 * SuperInstinct is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SuperInstinct is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SuperInstinct.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023 MelowehAndWelomeh
 */

package adris.altoclef.tasks.ArrowMapTests;

import java.util.Optional;

/**
 * SuperInstinct is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SuperInstinct is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SuperInstinct.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023 MelowehAndWelomeh
 */
public abstract class Bisection {
    public abstract double f(final double t);

    public boolean hasIntersection(final double left, final double right) {
        DebugPrint.println("hasIntersection: " + (Double.compare(Math.signum(f(left)), Math.signum(f(right))) != 0) + " left: " + left + " right: " + right + " f(left): " + f(left) + " f(right): " + f(right));
        return Double.compare(Math.signum(f(left)), Math.signum(f(right))) != 0;
    }

    public Optional<Double> optIteration(final double left, final double right) {
        final double f_left = f(left);
        final double f_right = f(right);
        return hasIntersection(left, right) ? Optional.of(iteration(left, right, f_left, f_right)) : Optional.empty();
    }

    /*public double iteration(final double left, final double right) {
        final double f_left = f(left);
        final double f_right = f(right);
        return iteration(left, right, f_left, f_right);
    } */
    public double iteration(final double left, final double right, final double f_left, final double f_right) {
        //DebugPrint.println("left: " + left + " right: " + right);
        //if ((int) Math.floor(left) <= (int) Math.ceil(right)) return left;
        final int leftInt = (int) Math.floor(left);
        final int rightInt = (int) Math.ceil(right);
        //TODO: test if rounding is necessary

        //double f_l = f(left);
        //double f_r = f(right);
        double mitte = (left + right) / 2;
        double f_mitte = f(mitte);
        DebugPrint.println("it unrounded: left=" + left + " right=" + right + " diff: "+ (left-right));
        DebugPrint.println("it rounded: left=" + leftInt + " right=" + rightInt + " diff: "+ (leftInt-rightInt));
        DebugPrint.println("intervall: " + f_left + ", " + f_right);
        DebugPrint.println("kleines intervall: " + f_left + ", " + f_mitte);
        //DebugPrint.println("signums: " + Math.signum(f_left) + ", " + Math.signum(f_mitte));
        DebugPrint.println("compare Zeug: " + (Double.compare(Math.signum(f_left), Math.signum(f_mitte)) != 0));


        if (rightInt - leftInt <= 1){
            //DebugPrint.println("bisection: " + left);
            return left;
        }

        if (rightInt - leftInt <= 2){
            //if (hasIntersection(left, (leftInt + 1))) {
            final double f_leftplus1 = f(leftInt + 1);
            if(Double.compare(Math.signum(f_left), Math.signum(f_leftplus1)) != 0){
                //DebugPrint.println("bisection: " + left);

                return left;
            }
            //DebugPrint.println("bisection: " + (leftInt + 1));

            return leftInt + 1;
        }

        //final double x = (left + right) / 2;
        //if (hasIntersection(left, x)){
        if(Double.compare(Math.signum(f_left), Math.signum(f_mitte)) != 0){
            DebugPrint.println("linkes Intervall");
            return iteration(left, mitte, f_left, f_mitte);
        }
        DebugPrint.println("rechtes Intervall");
        return iteration(mitte, right, f_mitte, f_right);
        //TODO: Optimierung f weitergeben wie Archimedes --> aktuell erledigt, sogar Bugfix
        //TODO: messy code cleanup, vor allem f_mitte später
    }
}
