import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'countryFilter'
})
export class CountryFilterPipe implements PipeTransform {
  transform(countries: string[], search: string): string[] {
    if (!countries) return [];
    if (!search) return countries;
    const filter = search.toLowerCase();
    return countries.filter(country => country.toLowerCase().includes(filter));
  }
}
