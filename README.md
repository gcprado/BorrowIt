# BorrowIt
Repository for the PDIGS course project at ULPGC

com.tuempresa.miclima/
├── data/
│   ├── model/
│   │   └── WeatherResponse.kt
│   ├── repository/
│   │   └── WeatherRepositoryImpl.kt
│   └── source.remote/
│       └── WeatherApiService.kt
├── domain/
│   ├── model/
│   │   └── City.kt
│   ├── repository/
│   │   └── WeatherRepository.kt
│   └── usecase/
│       ├── GetCityWeatherUseCase.kt
│       └── SearchCitiesUseCase.kt
├── presentation/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── components/
│   │   └── WeatherCard.kt
│   ├── screens/
│   │   ├── citylist/
│   │   │   ├── CityListScreen.kt
│   │   │   ├── CityListViewModel.kt
│   │   │   └── CityListUiState.kt
│   │   └── weatherdetail/
│   │       ├── WeatherDetailScreen.kt
│   │       ├── WeatherDetailViewModel.kt
│   │       └── WeatherDetailUiState.kt
│   └── navigation/
│       └── NavGraph.kt
├── di/
│   └── AppModule.kt
└── utils/
└── LocationUtils.kt