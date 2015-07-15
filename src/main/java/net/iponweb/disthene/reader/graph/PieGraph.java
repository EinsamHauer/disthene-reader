package net.iponweb.disthene.reader.graph;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.LogarithmicScaleNotAllowed;
import net.iponweb.disthene.reader.handler.parameters.RenderParameters;

import java.util.List;

/**
 * @author Andrei Ivanov
 */
public class PieGraph extends Graph {

    public PieGraph(RenderParameters renderParameters, List<TimeSeries> data) {
        super(renderParameters, data);
    }

    @Override
    public byte[] drawGraph() throws LogarithmicScaleNotAllowed {
        return getBytes();
    }
}
