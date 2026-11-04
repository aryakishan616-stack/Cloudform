const numberFormatter = new Intl.NumberFormat();
const API_BASE_URL = window.location.port && window.location.port !== "8080"
    ? "http://localhost:8080"
    : "";
let allCountries = [];

function toNumber(value) {
    const number = Number(value);
    return Number.isFinite(number) ? number : 0;
}

function getActiveCases(country) {
    if (country.active !== null && country.active !== undefined) {
        return toNumber(country.active);
    }

    return Math.max(
        toNumber(country.confirmed) - toNumber(country.deaths) - toNumber(country.recovered),
        0
    );
}

function formatNumber(value) {
    return numberFormatter.format(toNumber(value));
}

function renderCountries(countries) {
    const rows = document.getElementById("country-rows");
    rows.replaceChildren();
    const limit = document.getElementById("country-limit").value;
    const selectedCount = limit === "top20"
        ? Math.min(20, allCountries.length)
        : allCountries.length;
    const selectedDescription = limit === "top20" ? "top 20 by confirmed" : "all countries";
    document.getElementById("country-count").textContent =
        `Showing ${countries.length} of ${selectedCount} (${selectedDescription})`;

    if (countries.length === 0) {
        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 6;
        cell.textContent = allCountries.length === 0
            ? "No country data found. Add records to the database and refresh."
            : "No matching countries found. Try a different search.";
        row.appendChild(cell);
        rows.appendChild(row);
        return;
    }

    countries.forEach((country, index) => {
        const row = document.createElement("tr");
        const values = [
            index + 1,
            country.countryRegion || "Unknown",
            formatNumber(country.confirmed),
            formatNumber(country.deaths),
            formatNumber(country.recovered),
            formatNumber(getActiveCases(country))
        ];

        values.forEach((value) => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });

        rows.appendChild(row);
    });
}

function getFilteredCountries() {
    const searchTerm = document.getElementById("country-search").value.trim().toLocaleLowerCase();
    const limit = document.getElementById("country-limit").value;
    const sortBy = document.getElementById("country-sort").value;
    const [sortField, sortDirection] = sortBy.split("-");
    const multiplier = sortDirection === "desc" ? -1 : 1;
    const fields = {
        confirmed: (country) => toNumber(country.confirmed),
        deaths: (country) => toNumber(country.deaths),
        recovered: (country) => toNumber(country.recovered),
        active: getActiveCases
    };

    const selectedCountries = allCountries.slice();
    if (limit === "top20") {
        selectedCountries.sort((first, second) =>
            toNumber(second.confirmed) - toNumber(first.confirmed)
            || (first.countryRegion || "").localeCompare(
                second.countryRegion || "",
                undefined,
                { sensitivity: "base" }
            )
        );
        selectedCountries.splice(20);
    }

    return selectedCountries
        .filter((country) =>
            (country.countryRegion || "").toLocaleLowerCase().includes(searchTerm)
        )
        .slice()
        .sort((first, second) => {
            if (sortField === "country") {
                return (first.countryRegion || "").localeCompare(
                    second.countryRegion || "",
                    undefined,
                    { sensitivity: "base" }
                ) * multiplier;
            }

            return (fields[sortField](first) - fields[sortField](second)) * multiplier;
        });
}

function renderCountryChart(countries) {
    const svg = document.getElementById("country-chart");
    const metric = document.getElementById("chart-metric").value;
    const metricDetails = {
        confirmed: { label: "Confirmed cases", value: (country) => toNumber(country.confirmed) },
        deaths: { label: "Deaths", value: (country) => toNumber(country.deaths) },
        recovered: { label: "Recovered", value: (country) => toNumber(country.recovered) },
        active: { label: "Active cases", value: getActiveCases }
    };
    const selectedMetric = metricDetails[metric];
    const chartCountries = countries
        .slice()
        .sort((first, second) =>
            selectedMetric.value(second) - selectedMetric.value(first)
            || (first.countryRegion || "").localeCompare(
                second.countryRegion || "",
                undefined,
                { sensitivity: "base" }
            )
        )
        .slice(0, 10);
    const chartTitle = `Top ${chartCountries.length} countries by ${selectedMetric.label.toLocaleLowerCase()}`;
    const description = document.getElementById("chart-description");
    description.textContent = `${chartTitle} in the selected list.`;

    const title = document.createElementNS("http://www.w3.org/2000/svg", "title");
    title.textContent = chartTitle;
    svg.replaceChildren(title);
    svg.setAttribute("viewBox", "0 0 900 100");
    svg.setAttribute("aria-label", chartTitle);

    if (chartCountries.length === 0) {
        const emptyMessage = document.createElementNS("http://www.w3.org/2000/svg", "text");
        emptyMessage.setAttribute("x", "24");
        emptyMessage.setAttribute("y", "48");
        emptyMessage.setAttribute("class", "chart-empty");
        emptyMessage.textContent = "No matching country data to display.";
        svg.appendChild(emptyMessage);
        return;
    }

    const chartWidth = 900;
    const left = 170;
    const right = 95;
    const top = 16;
    const rowHeight = 38;
    const bottom = 42;
    const plotWidth = chartWidth - left - right;
    const plotHeight = chartCountries.length * rowHeight;
    const height = top + plotHeight + bottom;
    const maxValue = Math.max(...chartCountries.map(selectedMetric.value), 1);

    svg.setAttribute("viewBox", `0 0 ${chartWidth} ${height}`);

    for (let tick = 0; tick <= 4; tick += 1) {
        const x = left + (plotWidth * tick) / 4;
        const line = document.createElementNS("http://www.w3.org/2000/svg", "line");
        line.setAttribute("x1", String(x));
        line.setAttribute("x2", String(x));
        line.setAttribute("y1", String(top));
        line.setAttribute("y2", String(top + plotHeight));
        line.setAttribute("class", "chart-gridline");
        svg.appendChild(line);

        const tickLabel = document.createElementNS("http://www.w3.org/2000/svg", "text");
        tickLabel.setAttribute("x", String(x));
        tickLabel.setAttribute("y", String(top + plotHeight + 24));
        tickLabel.setAttribute("text-anchor", "middle");
        tickLabel.setAttribute("class", "chart-tick");
        tickLabel.textContent = formatNumber((maxValue * tick) / 4);
        svg.appendChild(tickLabel);
    }

    chartCountries.forEach((country, index) => {
        const value = selectedMetric.value(country);
        const y = top + index * rowHeight;
        const barWidth = (value / maxValue) * plotWidth;
        const countryName = country.countryRegion || "Unknown";

        const label = document.createElementNS("http://www.w3.org/2000/svg", "text");
        label.setAttribute("x", String(left - 12));
        label.setAttribute("y", String(y + 20));
        label.setAttribute("text-anchor", "end");
        label.setAttribute("class", "chart-country");
        label.textContent = countryName.length > 21
            ? `${countryName.slice(0, 19)}...`
            : countryName;
        const labelTitle = document.createElementNS("http://www.w3.org/2000/svg", "title");
        labelTitle.textContent = countryName;
        label.appendChild(labelTitle);
        svg.appendChild(label);

        const bar = document.createElementNS("http://www.w3.org/2000/svg", "rect");
        bar.setAttribute("x", String(left));
        bar.setAttribute("y", String(y + 4));
        bar.setAttribute("width", String(barWidth));
        bar.setAttribute("height", "22");
        bar.setAttribute("rx", "5");
        bar.setAttribute("class", `chart-bar chart-bar-${metric}`);
        svg.appendChild(bar);

        const valueLabel = document.createElementNS("http://www.w3.org/2000/svg", "text");
        valueLabel.setAttribute("x", String(left + barWidth + 8));
        valueLabel.setAttribute("y", String(y + 20));
        valueLabel.setAttribute("class", "chart-value");
        valueLabel.textContent = formatNumber(value);
        svg.appendChild(valueLabel);
    });
}

function renderFilteredCountries() {
    const countries = getFilteredCountries();
    renderCountries(countries);
    renderCountryChart(countries);
}

function renderTotals(countries) {
    const totals = countries.reduce((sum, country) => ({
        confirmed: sum.confirmed + toNumber(country.confirmed),
        deaths: sum.deaths + toNumber(country.deaths),
        recovered: sum.recovered + toNumber(country.recovered),
        active: sum.active + getActiveCases(country)
    }), { confirmed: 0, deaths: 0, recovered: 0, active: 0 });

    document.getElementById("confirmed-total").textContent = formatNumber(totals.confirmed);
    document.getElementById("deaths-total").textContent = formatNumber(totals.deaths);
    document.getElementById("recovered-total").textContent = formatNumber(totals.recovered);
    document.getElementById("active-total").textContent = formatNumber(totals.active);
}

async function loadCovidData() {
    const status = document.getElementById("status");
    const message = document.getElementById("message");
    const refreshButton = document.getElementById("refresh-button");

    status.textContent = "Loading data...";
    message.textContent = "";
    refreshButton.disabled = true;

    try {
        const refreshResponse = await fetch(`${API_BASE_URL}/api/covid/refresh`, {
            method: "GET",
            credentials: "include"
        });

        if (!refreshResponse.ok && refreshResponse.status !== 502) {
            throw new Error(`Refresh API returned HTTP ${refreshResponse.status}`);
        }

        const response = await fetch(`${API_BASE_URL}/api/covid/all`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error(`API returned HTTP ${response.status}`);
        }

        const countries = await response.json();
        if (!Array.isArray(countries)) {
            throw new Error("API response was not a list of country records");
        }

        allCountries = countries;
        renderFilteredCountries();
        renderTotals(countries);
        status.textContent = countries.length === 0
            ? "No data"
            : (refreshResponse.ok ? "Live data updated" : "Cached data available");

        if (countries.length === 0) {
            message.textContent = "The API is working, but no country records were found in the database.";
        }
    } catch (error) {
        status.textContent = "Data unavailable";
        message.textContent = `Could not load data. Check that the application and MySQL are running. ${error.message}`;
    } finally {
        refreshButton.disabled = false;
    }
}

document.getElementById("refresh-button").addEventListener("click", loadCovidData);
document.getElementById("logout-button").addEventListener("click", async () => {
    const status = document.getElementById("status");
    try {
        const csrfResponse = await fetch(`${API_BASE_URL}/api/user/csrf`, { credentials: "include" });
        if (!csrfResponse.ok) {
            throw new Error(`Could not get security token (HTTP ${csrfResponse.status})`);
        }

        const csrfToken = await csrfResponse.text();
        const response = await fetch(`${API_BASE_URL}/api/user/logout`, {
            method: "POST",
            headers: { "X-XSRF-TOKEN": csrfToken },
            credentials: "include"
        });
        if (!response.ok) {
            throw new Error(`Logout returned HTTP ${response.status}`);
        }

        window.location.assign("/index2.html");
    } catch (error) {
        console.error("Logout error:", error);
        status.textContent = "Could not log out. Please try again.";
    }
});

document.getElementById("country-search").addEventListener("input", renderFilteredCountries);
document.getElementById("country-limit").addEventListener("change", renderFilteredCountries);
document.getElementById("country-sort").addEventListener("change", renderFilteredCountries);
document.getElementById("chart-metric").addEventListener("change", renderFilteredCountries);
loadCovidData();