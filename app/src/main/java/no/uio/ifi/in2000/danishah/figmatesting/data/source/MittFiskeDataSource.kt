package no.uio.ifi.in2000.danishah.figmatesting.data.source

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.Loc
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.MittFiskeLocation
import no.uio.ifi.in2000.danishah.figmatesting.data.dataClasses.PointGeometry
import org.json.JSONArray

class MittFiskeDataSource(
    private val client: HttpClient
) {
    companion object {
        private const val BASE_URL = "https://www.mittfiske.no/umbraco/mittfiske/map/locations"
    }

    suspend fun fetchLocations(
        polygonWKT: String,
        pointWKT: String,
        min: Int,
        max: Int
    ): List<MittFiskeLocation> {
        return try {
            Log.d("MittFiske", "${polygonWKT}")
            val url = "$BASE_URL?" +
                    "filter=c/any(p:geo.intersects(p,geography'$polygonWKT')) " +
                    "and min le $min and max ge $max" +
                    "&limit=1000&orderby=geo.distance(p, geography'$pointWKT')"
            Log.d("MittFiske", "${url}")


            val response: HttpResponse = client.get(url)
            val raw = response.bodyAsText()


            val jsonArray = JSONArray(raw)
            val result = mutableListOf<MittFiskeLocation>()

            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)

                    val id = obj.getString("id")
                    var name = obj.getString("name")

                    val p = obj.getJSONObject("p")
                    val coordinates = p.getJSONArray("coordinates")
                    val point = PointGeometry(
                        type = p.getString("type"),
                        coordinates = listOf(
                            coordinates.getDouble(0),
                            coordinates.getDouble(1)
                        )
                    )

                    var locs = mutableListOf<Loc>()
                    val locsJson = obj.getJSONArray("locs")
                    for (j in 0 until locsJson.length()) {
                        val locJson = locsJson.getJSONObject(j)
                        locs.add(
                            Loc(
                                ca = locJson.getString("ca"),
                                n = locJson.getString("n"),
                                i = locJson.optString("i", null),
                                u = locJson.getString("u"),
                                k = locJson.getString("k"),
                                f = locJson.getString("f"),
                                fe = if (locJson.has("fe")) {
                                    val feArray = locJson.getJSONArray("fe")
                                    List(feArray.length()) { feArray.getString(it) }
                                } else null,
                                de = locJson.getString("de")
                            )
                        )
                    }
                    if (name == "2 fiskeplasser"){
                        locs = listOf(locs[1]).toMutableList()
                        name = locs[0].n
                    }

                    result.add(
                        MittFiskeLocation(
                            id = id,
                            name = name,
                            p = point,
                            locs = locs,
                            rating = null
                        )
                    )
                } catch (e: Exception) {
                    Log.e("MittFiske", "Error parsing element $i: ${e.message}")
                    continue
                }
            }

            Log.d("MittFiske", "DONE - number of spots: ${result.size}")
            result
        } catch (e: Exception) {
            Log.e("MittFiske", "Error fetching: ${e.message}")
            emptyList()
        }
    }
}
