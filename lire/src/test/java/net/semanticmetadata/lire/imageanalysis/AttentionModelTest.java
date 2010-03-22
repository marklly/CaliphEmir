/*
 * This file is part of Caliph & Emir.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2010 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */

package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.visualattention.StentifordModel;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Tests the use of the Stentiford Attention Model.
 * User: Mathias Lux
 * Date: 22.03.2010
 * Time: 16:36:57
 */
public class AttentionModelTest extends TestCase {
    public void testExtraction() throws IOException {
        StentifordModel sm = new StentifordModel();
        sm.extract(ImageIO.read(new File("wang-1000/103.jpg")));
        ImageIO.write(sm.getAttentionVisualization(), "png", new File("out.png"));
    }
}
